package com.cindrix.fitness

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object C {
    val bg = Color(0xFF05080F)
    val surf = Color(0xFF0B1322)
    val line = Color(0xFF1D3252)
    val txt = Color(0xFFF2F6FC)
    val mut = Color(0xFF9FB0C8)
    val acc = Color(0xFF4DA3FF)
    val b1 = Color(0xFF1F7CF0)
    val b2 = Color(0xFF0B5ED7)
    val green = Color(0xFF3CCB7F)
    val yellow = Color(0xFFF5C542)
    val red = Color(0xFFFF5A52)
}

fun fmt(d: Double): String = if (d == d.toLong().toDouble()) d.toLong().toString() else String.format("%.1f", d)

@Composable
fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(C.bg).verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun Header(title: String, sub: String? = null, onBack: (() -> Unit)? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Text("‹", color = C.txt, fontSize = 30.sp) }
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = C.txt, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            if (sub != null) Text(sub, color = C.mut, fontSize = 13.sp)
        }
        if (action != null) action()
    }
}

@Composable
fun CCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    pad: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val base = modifier.fillMaxWidth().clip(shape).background(C.surf)
        .border(BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) C.acc else C.line), shape)
    val m = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(m.padding(pad), content = content)
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val brush = if (enabled) Brush.verticalGradient(listOf(C.b1, C.b2)) else Brush.verticalGradient(listOf(C.line, C.line))
    Box(
        modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(12.dp)).background(brush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = if (enabled) Color.White else C.mut, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun OutlineButton(text: String, modifier: Modifier = Modifier, color: Color = C.acc, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, color, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
}

@Composable
fun Pill(text: String, color: Color = C.acc) {
    Text(
        text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.border(1.dp, color, RoundedCornerShape(999.dp)).padding(horizontal = 9.dp, vertical = 3.dp),
    )
}

@Composable
fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, color: Color = C.acc, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.height(44.dp).clip(shape)
            .then(if (selected) Modifier.background(Brush.verticalGradient(listOf(C.b1, C.b2))) else Modifier.background(C.surf))
            .border(if (selected) 0.dp else 1.dp, if (selected) Color.Transparent else C.line, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) Color.White else C.txt, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Bar(frac: Float, color: Color = C.acc) {
    val f = frac.coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(C.line)) {
        Box(Modifier.fillMaxWidth(f).height(7.dp).clip(RoundedCornerShape(4.dp))
            .background(Brush.horizontalGradient(listOf(C.b1, color))))
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = C.txt, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun Muted(text: String, size: Int = 13) {
    Text(text, color = C.mut, fontSize = size.sp, lineHeight = (size + 5).sp)
}

/** Shows the approved photo for an exercise, or a "Photo coming" placeholder. */
@Composable
fun ExercisePhoto(exId: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    val res = Library.photos[exId]
    val shape = RoundedCornerShape(if (compact) 8.dp else 12.dp)
    if (res != null) {
        Image(
            painter = painterResource(res), contentDescription = null, contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape).border(1.dp, C.line, shape),
        )
    } else {
        Box(
            modifier.clip(shape).background(C.surf).drawBehind {
                drawRoundRect(
                    color = C.mut, topLeft = Offset(1f, 1f), size = Size(size.width - 2f, size.height - 2f),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            Text("Photo coming", color = C.mut, fontSize = if (compact) 9.sp else 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun LineChart(values: List<Float>, labels: List<String>, modifier: Modifier = Modifier) {
    if (values.size < 2) {
        Box(modifier.height(110.dp), contentAlignment = Alignment.Center) { Muted("Log at least two entries to see a trend.") }
        return
    }
    val lo = values.min()
    val hi = values.max()
    val span = if (hi - lo < 0.001f) 1f else hi - lo
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(100.dp)) {
            val pad = 10f
            val w = size.width - pad * 2
            val h = size.height - pad * 2
            for (i in 0..2) {
                val y = pad + h * i / 2f
                drawLine(C.line, Offset(pad, y), Offset(pad + w, y), 1f)
            }
            val pts = values.mapIndexed { i, v ->
                Offset(pad + w * i / (values.size - 1), pad + h * (1f - (v - lo) / span))
            }
            val path = Path()
            pts.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
            drawPath(path, C.acc, style = Stroke(width = 5f))
            pts.forEach { drawCircle(C.acc, 7f, it) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(labels.first(), color = C.mut, fontSize = 11.sp)
            Text("${fmt(lo.toDouble())} – ${fmt(hi.toDouble())}", color = C.mut, fontSize = 11.sp)
            Text(labels.last(), color = C.mut, fontSize = 11.sp)
        }
    }
}

@Composable
fun RowSpace(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = content)
}

val ZERO_PAD = PaddingValues(0.dp)
val GAP = Modifier.width(8.dp)
