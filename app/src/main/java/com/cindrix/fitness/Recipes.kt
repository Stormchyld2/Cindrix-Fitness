package com.cindrix.fitness

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

data class Recipe(
    val id: String,
    val title: String,
    val cuisine: String,
    val type: String, // Breakfast, Main, Snack, Sauce
    val servings: Int,
    val minutes: Int?,
    val ingredients: List<String>,
    val steps: List<String>,
    val flags: List<String>,
    val origin: String, // orig, new, snack
    val group: String?,
    val nutrition: List<Double>?, // kcal, protein, carbs, fat, fiber per serving (provisional)
)

object Recipes {
    var all: List<Recipe> = emptyList()
        private set
    var byId: Map<String, Recipe> = emptyMap()
        private set

    fun load(ctx: Context) {
        if (all.isNotEmpty()) return
        try {
            val txt = ctx.assets.open("recipes.json").bufferedReader().use { it.readText() }
            val a = JSONArray(txt)
            all = (0 until a.length()).map { n ->
                val o = a.getJSONObject(n)
                fun strs(k: String): List<String> {
                    val x = o.optJSONArray(k) ?: return emptyList()
                    return (0 until x.length()).map { x.getString(it) }
                }
                val nu = o.optJSONArray("nu")
                Recipe(
                    o.getString("i"), o.getString("t"), o.optString("c"), o.optString("ty", "Main"),
                    o.optInt("s", 1).coerceAtLeast(1), if (o.isNull("m")) null else o.optInt("m"),
                    strs("g"), strs("p"), strs("f"), o.optString("o"),
                    if (o.has("k")) o.getString("k") else null,
                    if (nu == null) null else (0 until nu.length()).map { nu.getDouble(it) },
                )
            }
            byId = all.associateBy { it.id }
        } catch (e: Exception) {
            // Missing or unreadable data: the screens show an empty library instead of crashing.
        }
    }

    fun ofType(t: String): List<Recipe> = all.filter { it.type == t }
}

val RECIPE_TYPES = listOf("Breakfast", "Main", "Snack")
fun typeLabel(t: String): String = when (t) {
    "Breakfast" -> "Breakfast"
    "Main" -> "Lunch & Dinner"
    "Snack" -> "Snacks"
    else -> t
}

// How many favorites to tick per category: (low, target, high)
fun pickGuide(t: String): Triple<Int, Int, Int> = when (t) {
    "Breakfast" -> Triple(4, 5, 7)
    "Main" -> Triple(8, 10, 14)
    else -> Triple(5, 6, 10)
}

fun weekKeyFor(offset: Int): String = weekStart(LocalDate.now().plusWeeks(offset.toLong())).toString()

object MenuBuilder {
    // Slots: Breakfast, Lunch, Dinner, Snack per day. Lunch and dinner draw from the Main list.
    fun build(favs: Set<String>, previous: Set<String>, seed: Long): Map<String, String> {
        val out = mutableMapOf<String, String>()
        val rnd = Random(seed)
        fun pool(t: String) = Recipes.ofType(t).filter { it.id in favs }
            .shuffled(rnd).sortedBy { it.id in previous }
        val b = pool("Breakfast")
        val m = pool("Main")
        val s = pool("Snack")
        WEEK.forEachIndexed { d, day ->
            if (b.isNotEmpty()) out["$day|Breakfast"] = b[d % b.size].id
            if (m.isNotEmpty()) {
                out["$day|Lunch"] = m[(d * 2) % m.size].id
                out["$day|Dinner"] = m[(d * 2 + 1) % m.size].id
            }
            if (s.isNotEmpty()) out["$day|Snack"] = s[d % s.size].id
        }
        return out
    }
}

data class GItem(val key: String, val name: String, val qty: String, val group: String)

object Grocery {
    private val UNITS = mapOf(
        "cup" to "cup", "cups" to "cup", "tablespoon" to "tbsp", "tablespoons" to "tbsp", "tbsp" to "tbsp",
        "teaspoon" to "tsp", "teaspoons" to "tsp", "tsp" to "tsp", "g" to "g", "gram" to "g", "grams" to "g",
        "kg" to "kg", "oz" to "oz", "ounce" to "oz", "ounces" to "oz", "lb" to "lb", "lbs" to "lb",
        "pound" to "lb", "pounds" to "lb", "ml" to "ml", "l" to "l", "clove" to "clove", "cloves" to "clove",
        "can" to "can", "cans" to "can", "slice" to "slice", "slices" to "slice", "pinch" to "pinch",
    )
    private val FRAC = mapOf('¼' to 0.25, '½' to 0.5, '¾' to 0.75, '⅓' to 1.0 / 3, '⅔' to 2.0 / 3, '⅛' to 0.125)
    private const val NUM = "(\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:\\.\\d+)?[¼½¾⅓⅔⅛]?|[¼½¾⅓⅔⅛])"
    private val QTY = Regex("^\\s*$NUM(?:\\s*[–-]\\s*$NUM)?\\s*(.*)$")
    private val DROP = setOf("large", "medium", "small", "fresh", "chopped", "diced", "sliced", "minced", "shredded", "grated", "peeled", "of")

    private fun num(s: String): Double {
        val t = s.trim()
        if (t.length == 1 && FRAC.containsKey(t[0])) return FRAC.getValue(t[0])
        if (t.contains(' ') && t.contains('/')) {
            val (w, f) = t.split(Regex("\\s+"), limit = 2)
            val (a, b) = f.split('/')
            return w.toDouble() + a.toDouble() / b.toDouble()
        }
        if (t.contains('/')) {
            val (a, b) = t.split('/')
            return a.toDouble() / b.toDouble()
        }
        val last = t.last()
        if (FRAC.containsKey(last)) return t.dropLast(1).toDouble() + FRAC.getValue(last)
        return t.toDouble()
    }

    private fun singular(w: String): String = when {
        w.endsWith("ies") && w.length > 4 -> w.dropLast(3) + "y"
        (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes") || w.endsWith("sses")) -> w.dropLast(2)
        w.endsWith("s") && !w.endsWith("ss") && w.length > 3 -> w.dropLast(1)
        else -> w
    }

    private fun clean(name: String): String {
        var n = name.replace(Regex("\\([^)]*\\)"), " ").substringBefore(",")
        n = n.lowercase().replace(Regex("\\s+to taste"), "").replace(Regex("\\s+"), " ").trim()
        val words = n.split(' ').filter { it.isNotBlank() && it !in DROP }
        return words.joinToString(" ")
    }

    private val PRODUCE = setOf(
        "spinach", "pepper", "onion", "garlic", "mushroom", "lemon", "lime", "apple", "berry", "berries", "blueberry",
        "strawberry", "carrot", "cabbage", "zucchini", "broccoli", "potato", "parsley", "cilantro", "basil", "ginger",
        "bean", "pea", "fruit", "banana", "orange", "peach", "pear", "cherry", "pumpkin", "eggplant", "kale", "bok",
        "choy", "scallion", "shallot", "jalapeno", "corn", "arugula", "lettuce", "herb", "thyme", "rosemary", "dill",
        "mint", "okra", "collard", "squash", "asparagus", "radish", "leek", "fennel", "grape", "mango", "pineapple",
        "plum", "melon", "greens", "romaine", "chive", "sage", "oregano", "tarragon",
    )
    private val PROTEIN = setOf(
        "chicken", "turkey", "beef", "pork", "salmon", "cod", "shrimp", "scallop", "fish", "haddock", "trout", "swai",
        "crab", "sausage", "egg", "bacon", "ham", "steak", "tofu", "tenderloin", "sirloin", "lamb", "tilapia", "halibut",
        "mussel", "clam", "prosciutto", "jerky",
    )
    private val DAIRY = setOf(
        "yogurt", "cheese", "milk", "butter", "cream", "ricotta", "mozzarella", "parmesan", "skyr", "kefir", "cheddar",
        "gouda", "feta", "mascarpone", "ghee",
    )

    private fun groupOf(name: String): String {
        val words = name.split(Regex("[^a-z]+")).filter { it.isNotEmpty() }.map { singular(it) }
        val canned = name.contains("canned") || name.contains("dried") || name.contains("powder") ||
            name.contains("broth") || name.contains("stock") || name.contains("sauce") || name.contains("paste")
        if (words.any { it in DAIRY }) return "Dairy"
        if (words.any { it in PROTEIN } && !canned) return "Meat, fish and eggs"
        if (words.any { it in PRODUCE } && !canned && !name.contains("seed") && !name.contains("pepper flake")) return "Produce"
        return "Pantry"
    }

    private fun pluralUnit(u: String, q: Double): String = when {
        q > 1.0 && (u == "cup" || u == "clove" || u == "can" || u == "slice") -> u + "s"
        else -> u
    }

    private fun quarter(q: Double): String {
        val snapped = (q * 4).roundToInt().coerceAtLeast(1) / 4.0
        val whole = floor(snapped).toInt()
        val f = when (((snapped - whole) * 4).roundToInt()) {
            1 -> "¼"; 2 -> "½"; 3 -> "¾"; else -> ""
        }
        return if (whole == 0) f else "$whole$f"
    }

    private fun fmtQty(q: Double, unit: String): String = when (unit) {
        "" -> ceil(q - 1e-6).toInt().coerceAtLeast(1).toString()
        "g", "ml" -> q.roundToInt().coerceAtLeast(1).toString() + " " + unit
        "kg", "l", "oz", "lb" -> (Math.round(q * 10) / 10.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } + " " + unit
        "cup", "tbsp", "tsp" -> quarter(q) + " " + pluralUnit(unit, q)
        "pinch" -> "pinch"
        else -> ceil(q - 1e-6).toInt().coerceAtLeast(1).let { "$it " + pluralUnit(unit, it.toDouble()) }
    }

    private class Acc(val name: String, val unit: String) {
        var qty = 0.0
        var any = false
    }

    fun build(ids: List<String>, people: Int): List<GItem> {
        val acc = LinkedHashMap<String, Acc>()
        for (id in ids) {
            val r = Recipes.byId[id] ?: continue
            val factor = people.toDouble() / r.servings
            for (line in r.ingredients) {
                var qty: Double? = null
                var unit = ""
                var rest = line.trim()
                val m = QTY.find(rest)
                if (m != null) {
                    val a = num(m.groupValues[1])
                    val b = if (m.groupValues[2].isNotEmpty()) num(m.groupValues[2]) else a
                    qty = maxOf(a, b) * factor
                    rest = m.groupValues[3].trim()
                    val first = rest.substringBefore(' ').lowercase().trimEnd('.', ',')
                    UNITS[first]?.let { unit = it; rest = rest.substringAfter(' ', "").trim() }
                    if (rest.startsWith("of ")) rest = rest.removePrefix("of ")
                }
                val name = clean(rest)
                if (name.isBlank()) continue
                val key = unit + "|" + name.split(' ').joinToString(" ") { singular(it) }
                val a = acc.getOrPut(key) { Acc(name, unit) }
                if (qty != null) {
                    a.qty += qty
                    a.any = true
                }
            }
        }
        val order = listOf("Produce", "Meat, fish and eggs", "Dairy", "Pantry")
        return acc.map { (k, a) ->
            val display = a.name.replaceFirstChar { it.uppercase() }
            GItem(k, display, if (a.any) fmtQty(a.qty, a.unit) else "", groupOf(a.name))
        }.sortedWith(compareBy({ order.indexOf(it.group) }, { it.name }))
    }

    fun asText(title: String, items: List<GItem>): String {
        val sb = StringBuilder(title).append('\n')
        items.groupBy { it.group }.forEach { (g, l) ->
            sb.append('\n').append(g.uppercase()).append('\n')
            l.forEach { sb.append("- ").append(if (it.qty.isNotEmpty()) it.qty + " " else "").append(it.name).append('\n') }
        }
        return sb.toString()
    }
}
