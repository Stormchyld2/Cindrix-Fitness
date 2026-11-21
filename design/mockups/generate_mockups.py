import json
BG="#05080F";SURF="#0B1322";LINE="#1D3252";TXT="#F2F6FC";MUT="#9FB0C8";ACC="#4DA3FF";B1="#1F7CF0";B2="#0B5ED7"
LOGO="/_blob/29dc40b3f75596f3bcd7b56e1993b150"
P={"menu":'<path d="M4 7h16M4 12h16M4 17h16"/>',"bell":'<path d="M6 9a6 6 0 0 1 12 0c0 6 2 7 2 8H4c0-1 2-2 2-8zM10 21h4"/>',
"dumb":'<path d="M2 12h3M19 12h3M5 7v10M8 5v14M16 5v14M19 7v10M8 12h8"/>',"play":'<path d="M8 5l12 7-12 7z" fill="currentColor"/>',
"check":'<path d="M5 12l5 5 9-10"/>',"chev":'<path d="M9 5l7 7-7 7"/>',"back":'<path d="M15 5l-7 7 7 7"/>',
"home":'<path d="M3 11l9-8 9 8v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1z"/>',"cal":'<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/>',
"chart":'<path d="M4 20v-6M10 20V6M16 20v-9M22 20H2"/>',"book":'<path d="M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2zM4 21V5M9 7h6"/>',
"search":'<circle cx="11" cy="11" r="7"/><path d="M21 21l-5-5"/>',"down":'<path d="M6 9l6 6 6-6"/>',
"person":'<circle cx="12" cy="5" r="2.5"/><path d="M12 8v6M12 14l-4 6M12 14l4 6M7 11l5-3 5 3"/>'}
def ic(n,s=24,c="currentColor"):
    return f'<svg width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="{c}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{P[n]}</svg>'
HEAD='''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>@@T@@</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&amp;display=swap">
<style>
body{margin:0}
a{text-decoration:none;color:inherit}
button,input{font-family:inherit}
</style>
</helmet>
<div style="width:390px;height:844px;box-sizing:border-box;background:'''+BG+''';color:'''+TXT+''';font-family:'Inter',system-ui,sans-serif;display:flex;flex-direction:column;overflow:hidden">
'''
FOOT='''</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{"$preview":{"width":390,"height":844}}'>
class Component extends DCLogic {
renderVals() {
return {};
}
}
</script>
</body>
</html>
'''
def tabs(active):
    it=[("home","Today","Main.dc.html"),("cal","Program","Program.dc.html"),("dumb","Workouts","Workout.dc.html"),("chart","Progress","Progress.dc.html"),("book","Library","Library.dc.html")]
    o=f'<nav aria-label="Main" style="display:flex;border-top:1px solid {LINE};background:{BG};height:72px;flex:none">'
    for k,l,h in it:
        c=ACC if l==active else MUT
        o+=f'<a href="{h}" style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;color:{c};font-size:11px;font-weight:600;min-height:44px">{ic(k,22)}<span>{l}</span></a>'
    return o+'</nav>\n'
def card(inner,sel=False,pad=14,extra=""):
    b=f"1.5px solid {ACC}" if sel else f"1px solid {LINE}"
    sh="0 0 16px rgba(43,140,255,0.35)" if sel else "0 1px 0 rgba(255,255,255,0.04) inset"
    return f'<div style="background:{SURF};border:{b};border-radius:14px;padding:{pad}px;box-shadow:{sh};{extra}">{inner}</div>\n'
def gbtn(label,href,icon=None,h=52):
    i=ic(icon,20) if icon else ""
    return f'<a href="{href}" style="display:flex;align-items:center;justify-content:center;gap:8px;height:{h}px;border-radius:12px;background:linear-gradient(180deg,{B1},{B2});box-shadow:0 4px 14px rgba(11,94,215,0.45);color:#fff;font-size:17px;font-weight:700">{i}{label}</a>\n'
def seg(items,active):
    o=f'<div style="display:flex;background:{SURF};border:1px solid {LINE};border-radius:10px;padding:3px;gap:2px">'
    for t in items:
        if t==active: o+=f'<div style="flex:1;height:34px;border-radius:8px;background:linear-gradient(180deg,{B1},{B2});display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:700;color:#fff">{t}</div>'
        else: o+=f'<div style="flex:1;height:34px;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:500;color:{MUT}">{t}</div>'
    return o+'</div>\n'
def title(t,icon=None,back=None,sub=None):
    o='<div style="padding:16px 16px 10px;flex:none">'
    if back: o+=f'<a href="{back}" aria-label="Back" style="display:flex;align-items:center;justify-content:center;width:44px;height:44px;margin:-8px 0 0 -12px">{ic("back")}</a>'
    row=f'<div style="display:flex;align-items:center;gap:10px">'+(f'<span style="color:{ACC}">{ic(icon,26)}</span>' if icon else '')+f'<h1 style="margin:0;font-size:26px;font-weight:800;line-height:1.15">{t}</h1></div>'
    o+=row
    if sub: o+=f'<div style="margin-top:6px;font-size:13px;color:{MUT}">{sub}</div>'
    return o+'</div>\n'
def mount(w=390,h=150,op=1):
    return f'<svg viewBox="0 0 {w} {h}" preserveAspectRatio="xMidYMid slice" width="{w}" height="{h}" aria-hidden="true" style="position:absolute;left:0;top:0;width:100%;height:100%;opacity:{op}"><defs><linearGradient id="sky" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#0A1A33"/><stop offset="1" stop-color="#16427F"/></linearGradient></defs><rect width="{w}" height="{h}" fill="url(#sky)"/><path d="M0 105L60 60L95 85L150 35L215 92L262 55L330 98L390 70V150H0z" fill="#0B1E3C"/><path d="M0 125L70 90L120 112L190 74L250 116L320 88L390 120V150H0z" fill="#050B16"/></svg>'
# ===== Hybrid app screens =====
G="#3CCB7F";Y="#F5C542";R="#FF5A52"
P.update({"star":'<path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>',
"shield":'<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/>',"bolt":'<path d="M13 2L4 14h7l-1 8 9-12h-7z"/>',
"trophy":'<path d="M8 4h8v5a4 4 0 0 1-8 0zM8 6H4v2a3 3 0 0 0 4 3M16 6h4v2a3 3 0 0 1-4 3M12 13v4M8 21h8M10 17h4"/>',
"lock":'<rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>',
"scale":'<rect x="4" y="4" width="16" height="16" rx="3"/><path d="M8 9a4 4 0 0 1 8 0M12 9l2-2"/>',
"tape":'<rect x="3" y="8" width="18" height="8" rx="1"/><path d="M7 8v4M11 8v3M15 8v4M19 8v3"/>',
"ball":'<circle cx="12" cy="12" r="8"/><path d="M6 8c4 2 8 2 12 0M6 16c4-2 8-2 12 0"/>',
"moon":'<path d="M20 14a8 8 0 1 1-10-10 6 6 0 0 0 10 10z"/>',
"fork":'<path d="M7 3v8M5 3v5a2 2 0 0 0 4 0V3M7 11v10M17 3c-2 2-3 5-3 8h3v10"/>',
"close":'<path d="M6 6l12 12M18 6L6 18"/>',"clip":'<rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4h6v3H9zM8 12h8M8 16h6"/>',
"camera":'<path d="M4 8h3l2-3h6l2 3h3v11H4z"/><circle cx="12" cy="13" r="3.5"/>',"heart":'<path d="M12 21C5 16 3 12 3 9a5 5 0 0 1 9-3 5 5 0 0 1 9 3c0 3-2 7-9 12z"/>'})
IMG={'chest_press': '/_blob/37f2f6c320056e03209dbdb68bd83d5d', 'seated_row': '/_blob/90e36b855e98918796ae6c382688b020', 'lat_pulldown': '/_blob/baa1746ad915b960c684486c5d42933d', 'triceps_pressdown': '/_blob/e9397be64e50e52da590ab38a0ea4192', 'bar_curl': '/_blob/a5f44111a3e181077cf108cc2cfb6f73', 'band_bar_squat': '/_blob/5fc68aac66f99216168f62ccbd88c327', 'pull_through': '/_blob/e5fbaccd91eddfc6d3a4a098cf27d021', 'hamstring_curl': '/_blob/69f12909b797d2194eb1710723ffba1b', 'glute_kickback': '/_blob/7b5f921cb69776422645375f467d284b', 'pallof_press': '/_blob/c83b4e01aea3cde17d05f69d4c361c53', 'overhead_press': '/_blob/9fc8630388ec6b46cbe02ed10d2101d7', 'face_pull': '/_blob/216a0df5307cd39b4fbb4dab067364ba'}
def thumb(k,s=48,r=8):
    if k not in IMG:
        return f'<div style="width:{s}px;height:{s}px;flex:none;border-radius:{r}px;border:1px dashed {LINE};display:flex;align-items:center;justify-content:center;color:{MUT}">{ic("camera",max(16,s//3))}</div>'
    return f'<div style="width:{s}px;height:{s}px;flex:none;border-radius:{r}px;overflow:hidden;border:1px solid {LINE};background:#fff"><img src="{IMG[k]}" alt="" style="display:block;width:{2*s}px;height:{s}px;max-width:none;object-fit:cover;object-position:left center"></div>'
def head(t,extra=""):
    return HEAD.replace("@@T@@",t).replace("display:flex;flex-direction:column;overflow:hidden","display:flex;flex-direction:column;overflow:hidden;"+extra,1)
def pill(t,c=None):
    c=c or ACC
    return f'<span style="font-size:11px;font-weight:700;color:{c};border:1px solid {c};border-radius:999px;padding:3px 9px;white-space:nowrap">{t}</span>'
def bar(pct,h=6,col=None):
    return f'<div style="height:{h}px;border-radius:{h//2}px;background:{LINE}"><div style="width:{pct}%;height:{h}px;border-radius:{h//2}px;background:linear-gradient(90deg,{B1},{col or ACC})"></div></div>'
files={}
# ---- Today
b=head("Cindrix Fitness - Today")
b+=f'<div style="display:flex;align-items:center;justify-content:space-between;padding:8px 8px;flex:none"><button aria-label="Menu" style="width:44px;height:44px;background:none;border:0;color:{TXT}">{ic("menu")}</button><div style="display:flex;align-items:center;gap:6px"><div style="width:40px;height:40px;overflow:hidden;border-radius:8px"><img src="{LOGO}" alt="Cindrix Fitness logo" style="width:64px;height:64px;margin:-4px 0 0 -12px;display:block"></div><span style="font-size:15px;font-weight:800;font-style:italic;letter-spacing:0.04em">CINDRIX <span style="color:{ACC}">FITNESS</span></span></div><button aria-label="Notifications" style="width:44px;height:44px;background:none;border:0;color:{TXT}">{ic("bell")}</button></div>\n'
b+=f'<div style="position:relative;height:104px;flex:none;overflow:hidden">{mount()}<div style="position:absolute;left:16px;bottom:10px"><div style="font-size:30px;font-weight:800">Today</div><div style="font-size:12px;color:{MUT};margin-top:2px">Discipline today. A stronger tomorrow.</div></div></div>\n'
b+='<div style="flex:1;padding:10px 16px 0;display:flex;flex-direction:column;gap:10px;min-height:0">\n'
b+=f'<a href="Rewards.dc.html" style="display:block;background:{SURF};border:1px solid {LINE};border-radius:14px;padding:10px 12px"><div style="display:flex;align-items:center;gap:10px"><div style="width:40px;height:40px;border-radius:50%;border:2px solid {ACC};display:flex;align-items:center;justify-content:center;color:{ACC}">{ic("shield",22)}</div><div style="flex:1"><div style="display:flex;justify-content:space-between;align-items:baseline"><span style="font-size:15px;font-weight:800">Operator · Level 3</span><span style="font-size:12px;color:{MUT}">640 / 1,000 XP</span></div><div style="margin-top:6px">{bar(64)}</div></div></div><div style="display:flex;gap:8px;margin-top:8px">{pill("Week streak 4")}{pill("Sample data",MUT)}</div></a>\n'
b+=card(f'<div style="display:flex;align-items:center;gap:12px;margin-bottom:10px">{thumb("chest_press",52)}<div><div style="font-size:12px;color:{MUT}">Week 5 of 12 · Monday</div><div style="font-size:19px;font-weight:800">Chest + Back + Arms</div><div style="font-size:12px;color:{MUT}">Plus slam ball and heavy bag</div></div></div>'+gbtn("Start Workout","Workout.dc.html","play",50),pad=12)
days=[("Mon","Chest",True),("Tue","Rest",False),("Wed","Legs",False),("Thu","Rest",False),("Fri","Full",False),("Sat","Rest",False),("Sun","Rest",False)]
w=f'<div style="font-size:13px;font-weight:700;margin-bottom:6px">This Week</div><div style="display:flex;gap:5px">'
for d,l,on in days:
    bd=f"1.5px solid {ACC}" if on else f"1px solid {LINE}"
    c=TXT if l!="Rest" else MUT
    w+=f'<div style="flex:1;border:{bd};border-radius:8px;background:{SURF};padding:6px 0;text-align:center;min-height:44px;box-sizing:border-box"><div style="font-size:10px;color:{MUT}">{d}</div><div style="font-size:11px;font-weight:700;color:{c};margin-top:3px">{l}</div></div>'
b+='<div>'+w+'</div></div>\n'
q=f'<div style="display:flex;justify-content:space-between;align-items:baseline;margin-bottom:8px"><span style="font-size:15px;font-weight:700">Weekly quests</span><span style="font-size:12px;color:{MUT}">+150 XP when all done</span></div><div style="display:flex;flex-direction:column;gap:8px">'
for l,v,p in [("3 workouts","1 of 3",33),("Protein target, 5 days","2 of 5",40),("Walking target, 4 days","1 of 4",25)]:
    q+=f'<div><div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:4px"><span>{l}</span><span style="color:{MUT}">{v}</span></div>{bar(p,5)}</div>'
b+=card(q+'</div>',pad=12)
b+='</div>\n'+tabs("Today")+FOOT
files["Main.dc.html"]=b
# ---- Program
b=head("Cindrix Fitness - Program")+title("Program","cal",sub="Hybrid Muscle, Fat Loss and Boxing · 12 weeks")
b+='<div style="flex:1;padding:0 16px;display:flex;flex-direction:column;gap:10px;min-height:0">\n'+seg(["Week","Checkpoints","Rules"],"Week")
for k,d,t,s,sel in [("chest_press","Monday","Chest + Back + Arms","Heavy bag · 4 rounds",True),("band_bar_squat","Wednesday","Legs + Glutes + Core","Boxing machine · 4 rounds",False),("overhead_press","Friday","Full Body + Shoulders + Chest","Heavy bag · hardest round",False)]:
    right=f'<div style="width:28px;height:28px;border-radius:50%;background:linear-gradient(180deg,{B1},{B2});display:flex;align-items:center;justify-content:center;color:#fff">{ic("check",18)}</div>' if sel else ic("chev",20,MUT)
    b+=card(f'<div style="display:flex;align-items:center;gap:12px">{thumb(k,64,10)}<div style="flex:1;min-width:0"><div style="font-size:12px;font-weight:700;color:{ACC}">{d.upper()}</div><div style="font-size:16px;font-weight:800;margin-top:1px">{t}</div><div style="font-size:12px;color:{MUT};margin-top:2px">{s} · slam ball</div></div>{right}</div>',sel=sel,pad=10)
b+=card(f'<div style="font-size:14px;font-weight:700;margin-bottom:6px">Boxing and slam-ball progression</div><div style="display:flex;flex-direction:column;gap:5px;font-size:12px;color:{MUT}"><div><b style="color:{TXT}">Weeks 1–4</b> · 4 rounds, 30 s rest · slam 2 rounds</div><div><b style="color:{TXT}">Weeks 5–8</b> · boxing rest down to 20 s</div><div><b style="color:{TXT}">Weeks 9–12</b> · 5th round on Friday first</div></div>',pad=12)
b+='</div>\n'+tabs("Program")+FOOT
files["Program.dc.html"]=b
# ---- Workout list
def wrow(k,name,meta,right=None,icon=None,flag=False):
    lead=thumb(k,44) if k else f'<div style="width:44px;height:44px;flex:none;border:1px solid {LINE};border-radius:8px;display:flex;align-items:center;justify-content:center;color:{ACC}">{ic(icon,20)}</div>'
    fl=pill("Band up") if flag else ''
    r=f'<span style="font-size:12px;color:{MUT};white-space:nowrap">{right}</span>' if right else ic("chev",18,MUT)
    href="Modal.dc.html" if k=="chest_press" else "Modal.dc.html"
    return f'<a href="{href}" style="display:flex;align-items:center;gap:10px;background:{SURF};border:1px solid {LINE};border-radius:12px;padding:5px 10px;min-height:44px;box-sizing:border-box">{lead}<div style="flex:1;min-width:0"><div style="font-size:14px;font-weight:700">{name}</div><div style="font-size:12px;color:{MUT}">{meta}</div></div>{fl}{r}</a>\n'
def wlist():
    o=wrow(None,"Warm-up","Shadowboxing, shoulder rolls","5 min",icon="person")
    for k,n,m,f in [("chest_press","Standing band chest press","3 × 10–15 · 15 lb per side",True),("seated_row","Seated band row","3 × 10–15 · 25 lb per side",False),("lat_pulldown","Lat pulldown","3 × 10–15 · 15 lb per side",False),("triceps_pressdown","Triceps pressdown","3 × 10–15 · 25 lb total",False),("bar_curl","Weighted-bar band curl","3 × 10–15 · 25 lb + bar",False),("slam_ball","Slam ball","2 rounds · 60 s / 30 s · 10 lb",False),("heavy_bag","Heavy bag","4 rounds · 2 min / 30 s",False)]:
        o+=wrow(k,n,m,flag=f)
    o+=wrow(None,"Cooldown","Walk, then stretch","4 min",icon="person")
    return o
b=head("Cindrix Fitness - Workout")+title("Monday",back="Main.dc.html",sub="Chest + Back + Arms + Heavy Bag · tap an exercise for how-to")
b+='<div style="flex:1;padding:0 16px;display:flex;flex-direction:column;gap:5px;min-height:0">\n'+wlist()
b+='<div style="margin-top:auto;padding:4px 0 8px">'+gbtn("Start Workout","Exercise.dc.html","play",50)+'</div>\n</div>\n'+tabs("Workouts")+FOOT
files["Workout.dc.html"]=b
# ---- Modal
b=head("Cindrix Fitness - Exercise how-to","position:relative")
b+=title("Monday",sub="Chest + Back + Arms + Heavy Bag")+'<div style="padding:0 16px;display:flex;flex-direction:column;gap:5px">'+wlist()+'</div>\n'
b+='<div style="position:absolute;left:0;top:0;width:390px;height:844px;background:rgba(2,5,10,0.72)"></div>\n'
steps=["Anchor the middle D-ring, clip on handles, one 15 lb band on each side.","Take a comfortable staggered stance. Keep ribs controlled and abdomen lightly braced.","Keep shoulders down and wrists neutral.","Press forward without leaning back, then control the return."]
sh=f'<div style="position:absolute;left:0;top:118px;width:390px;height:726px;box-sizing:border-box;background:{SURF};border-top:1px solid {ACC};border-radius:20px 20px 0 0;padding:8px 16px 16px;display:flex;flex-direction:column;gap:10px;box-shadow:0 -8px 30px rgba(43,140,255,0.25)">'
sh+=f'<div style="width:44px;height:5px;border-radius:3px;background:{LINE};margin:0 auto"></div>'
sh+=f'<div style="display:flex;align-items:flex-start;justify-content:space-between"><div><div style="font-size:12px;font-weight:700;color:{ACC}">EXERCISE 1</div><div style="font-size:22px;font-weight:800;line-height:1.15">Standing band chest press</div></div><a href="Workout.dc.html" aria-label="Close" style="width:44px;height:44px;margin:-6px -10px 0 0;display:flex;align-items:center;justify-content:center;color:{TXT}">{ic("close")}</a></div>'
sh+=f'<div style="background:#fff;border-radius:12px;padding:6px;text-align:center"><img src="{IMG["chest_press"]}" alt="Two photos of the standing band chest press: start position and the pressed position" style="display:block;margin:0 auto;width:100%;height:auto"></div>'
sh+=f'<div style="display:flex;gap:6px;flex-wrap:wrap">{pill("Middle D-ring")}{pill("15 lb per side")}{pill("3 × 10–15")}{pill("Rest 60–90 s")}</div>'
sh+=f'<div style="font-size:13px;font-weight:700">How to do it</div><div style="display:flex;flex-direction:column;gap:6px;font-size:14px;line-height:1.35">'
for i,t in enumerate(steps,1): sh+=f'<div style="display:flex;gap:10px"><span style="color:{ACC};font-weight:800;width:12px">{i}</span><span>{t}</span></div>'
sh+='</div>'
sh+=f'<div style="border:1px solid {LINE};border-radius:10px;padding:8px 10px;font-size:13px;color:{MUT}"><b style="color:{TXT}">Substitute:</b> single-arm chest press with lighter resistance.</div>'
sh+=f'<div style="display:flex;gap:8px;margin-top:auto"><a href="Modal.dc.html" style="flex:1;display:flex;align-items:center;justify-content:center;gap:6px;height:50px;border-radius:12px;border:1px solid {ACC};color:{ACC};font-size:15px;font-weight:700">{ic("play",16)} Demo video</a><a href="Exercise.dc.html" style="flex:1;display:flex;align-items:center;justify-content:center;height:50px;border-radius:12px;background:linear-gradient(180deg,{B1},{B2});color:#fff;font-size:15px;font-weight:700">Start set</a></div></div>\n'
b+=sh+FOOT
files["Modal.dc.html"]=b
# ---- Active set
b=head("Cindrix Fitness - Active set")
b+=f'<div style="position:relative;height:104px;flex:none;overflow:hidden;background:#fff"><img src="{IMG["chest_press"]}" alt="" style="display:block;height:104px;width:100%;object-fit:cover;object-position:left center"><a href="Workout.dc.html" aria-label="Back" style="position:absolute;left:4px;top:6px;width:44px;height:44px;border-radius:50%;background:rgba(5,8,15,0.75);display:flex;align-items:center;justify-content:center;color:{TXT}">{ic("back")}</a><a href="Modal.dc.html" aria-label="How to do this exercise" style="position:absolute;right:10px;bottom:8px;display:flex;align-items:center;gap:6px;height:44px;padding:0 14px;border-radius:999px;background:rgba(5,8,15,0.8);border:1px solid {ACC};color:{TXT};font-size:13px;font-weight:700">{ic("play",16)} How-to</a></div>\n'
b+='<div style="flex:1;padding:10px 16px 0;display:flex;flex-direction:column;gap:9px;min-height:0">\n'
b+=f'<div><div style="font-size:24px;font-weight:800">Standing band chest press</div><div style="display:flex;align-items:center;gap:8px;margin-top:4px"><span style="font-size:13px;font-weight:700">Set 2 of 3</span>'
for c in ["#5A6B85",ACC,LINE]: b+=f'<span style="width:30px;height:5px;border-radius:3px;background:{c}"></span>'
b+=f'<span style="margin-left:auto;font-size:12px;color:{MUT}">Target 10–15 reps</span></div></div>\n'
b+=f'<div><div style="font-size:12px;font-weight:600;color:{MUT};margin-bottom:5px">Band (lb per side)</div><div style="display:flex;gap:5px">'
for l,on in [("15",True),("25",False),("35",False),("45",False),("55",False),("Stack",False)]:
    if on: b+=f'<button style="flex:1;height:40px;border-radius:8px;border:0;background:linear-gradient(180deg,{B1},{B2});color:#fff;font-size:13px;font-weight:700">{l}</button>'
    else: b+=f'<button style="flex:1;height:40px;border-radius:8px;border:1px solid {LINE};background:{SURF};color:{TXT};font-size:13px;font-weight:600">{l}</button>'
b+='</div></div>\n'
b+=card(f'<div style="display:flex;align-items:center;justify-content:space-between"><button aria-label="Fewer reps" style="width:50px;height:50px;border-radius:12px;border:1px solid {LINE};background:{BG};color:{TXT};font-size:26px">−</button><div style="text-align:center"><div style="font-size:44px;font-weight:800;line-height:1">12</div><div style="font-size:12px;color:{MUT}">reps</div></div><button aria-label="More reps" style="width:50px;height:50px;border-radius:12px;border:1px solid {LINE};background:{BG};color:{TXT};font-size:26px">+</button></div>',pad=8)
b+=f'<div><div style="font-size:12px;font-weight:600;color:{MUT};margin-bottom:5px">Reps left in reserve</div><div style="display:flex;gap:5px">'
for l,on in [("0",False),("1",True),("2",False),("3+",False)]:
    if on: b+=f'<button style="flex:1;height:40px;border-radius:8px;border:1.5px solid {ACC};background:{SURF};color:{TXT};font-size:14px;font-weight:700">{l}</button>'
    else: b+=f'<button style="flex:1;height:40px;border-radius:8px;border:1px solid {LINE};background:{SURF};color:{MUT};font-size:14px;font-weight:600">{l}</button>'
b+='</div></div>\n'
b+=f'<div><div style="font-size:12px;font-weight:600;color:{MUT};margin-bottom:5px">Joints this set</div><div style="display:flex;gap:5px">'
for l,c,sym in [("Green",G,"●"),("Yellow",Y,"▲"),("Red",R,"■")]:
    on=l=="Green"
    bd=f"1.5px solid {c}" if on else f"1px solid {LINE}"
    b+=f'<button style="flex:1;height:40px;border-radius:8px;border:{bd};background:{SURF};color:{TXT};font-size:13px;font-weight:700"><span style="color:{c}">{sym}</span> {l}</button>'
b+='</div></div>\n'
b+='<div style="margin-top:auto;padding-bottom:8px">'+gbtn("Log Set  +10 XP","Workout.dc.html","check",50)+f'<div style="text-align:center;font-size:12px;color:{MUT};margin-top:6px">Rest 60–90 s before the next set</div></div>\n</div>\n'+tabs("Workouts")+FOOT
files["Exercise.dc.html"]=b
# ---- Progress
b=head("Cindrix Fitness - Progress")+title("Progress","chart",sub="Sample data for layout only")
b+='<div style="flex:1;padding:0 16px;display:flex;flex-direction:column;gap:9px;min-height:0">\n'+seg(["Week","Month","All"],"Month")
ring=f'<svg width="84" height="84" viewBox="0 0 104 104" role="img" aria-label="36 percent of sessions done"><circle cx="52" cy="52" r="44" fill="none" stroke="{LINE}" stroke-width="10"/><circle cx="52" cy="52" r="44" fill="none" stroke="{ACC}" stroke-width="10" stroke-linecap="round" stroke-dasharray="100 277" transform="rotate(-90 52 52)"/><text x="52" y="58" text-anchor="middle" font-size="24" font-weight="800" fill="{TXT}" font-family="Inter,sans-serif">36%</text></svg>'
b+=card(f'<div style="display:flex;align-items:center;gap:14px">{ring}<div style="flex:1"><div style="font-size:15px;font-weight:700">Workout Completion</div><div style="font-size:12px;color:{MUT};margin-top:2px">13 of 36 sessions · week 5 of 12</div><div style="font-size:12px;color:{MUT};margin-top:2px">Next checkpoint: week 8</div></div></div>',pad=12)
pts=[(30,60),(90,66),(150,76),(210,86),(270,92)]
poly=" ".join(f"{x},{y}" for x,y in pts)
dots="".join(f'<circle cx="{x}" cy="{y}" r="4" fill="{ACC}"/>' for x,y in pts)
lab="".join(f'<text x="{x}" y="116" text-anchor="middle" font-size="11" fill="{MUT}" font-family="Inter,sans-serif">W{i+1}</text>' for i,(x,y) in enumerate(pts))
grid="".join(f'<line x1="10" x2="300" y1="{y}" y2="{y}" stroke="{LINE}" stroke-width="1"/>' for y in (30,60,90))
chart=f'<svg viewBox="0 0 310 122" width="100%" height="110" role="img" aria-label="Weekly average body weight trending down over five weeks (sample data)">{grid}<polyline points="{poly}" fill="none" stroke="{ACC}" stroke-width="3" stroke-linejoin="round"/>{dots}{lab}</svg>'
b+=card(f'<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px"><div style="font-size:15px;font-weight:700">Weekly average weight</div><div style="font-size:12px;color:{MUT}">sample</div></div>{chart}',pad=12)
bl=f'<div style="font-size:15px;font-weight:700;margin-bottom:8px">Band levels</div><div style="display:flex;flex-direction:column;gap:8px">'
for n,l,v,f in [("Chest press","Lv 2","25 lb per side",True),("Seated row","Lv 3","35 lb per side",False),("Squat","Lv 2","25 lb per side + bar",False)]:
    bl+=f'<div style="display:flex;align-items:center;gap:10px"><div style="width:34px;height:34px;border-radius:8px;border:1px solid {ACC};display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:800;color:{ACC}">{l}</div><div style="flex:1"><div style="font-size:14px;font-weight:600">{n}</div><div style="font-size:12px;color:{MUT}">{v}</div></div>{pill("Band up") if f else ""}</div>'
b+=card(bl+'</div>',pad=12)
b+='<a href="Track.dc.html" style="display:flex;align-items:center;justify-content:center;gap:8px;height:48px;border-radius:12px;border:1px solid '+ACC+';color:'+ACC+';font-size:15px;font-weight:700">Open trackers '+ic("chev",18)+'</a>\n</div>\n'+tabs("Progress")+FOOT
files["Progress.dc.html"]=b
# ---- Trackers
b=head("Cindrix Fitness - Trackers")+title("Trackers","clip",back="Progress.dc.html",sub="Tap a tracker to log today")
b+='<div style="flex:1;padding:0 16px;display:flex;flex-direction:column;gap:10px;min-height:0">\n'+seg(["Essentials","All"],"All")
b+='<div style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px">\n'
for ic_,n,v,href in [("scale","Weight","Weekly avg · sample","Track.dc.html"),("tape","Measurements","Waist, chest, hips, arm","Track.dc.html"),("dumb","Band progression","12 exercises","Progress.dc.html"),("clip","Workout log","Sets, reps, bands","Track.dc.html"),("bolt","Boxing","Rounds · intensity 1–10","Track.dc.html"),("ball","Slam ball","Rounds · power held","Track.dc.html"),("person","Joint and pain","Back · knee · shoulder · ankle","Joint.dc.html"),("moon","Recovery","Sleep · energy · resting HR","Track.dc.html"),("fork","Nutrition","Calories · protein · water","Track.dc.html"),("heart","Steps and walking","Daily target","Track.dc.html")]:
    b+=f'<a href="{href}" style="display:block;background:{SURF};border:1px solid {LINE};border-radius:12px;padding:10px 12px;min-height:44px"><div style="color:{ACC}">{ic(ic_,22)}</div><div style="font-size:14px;font-weight:700;margin-top:6px">{n}</div><div style="font-size:11px;color:{MUT};margin-top:2px">{v}</div></a>\n'
b+='</div>\n</div>\n'+tabs("Progress")+FOOT
files["Track.dc.html"]=b
# ---- Joint
b=head("Cindrix Fitness - Joint and pain tracker")+title("Joint and Pain",back="Track.dc.html",sub="Rate each joint before and after training")
b+='<div style="flex:1;padding:0 16px;display:flex;flex-direction:column;gap:9px;min-height:0">\n'
def zone(sel,sym_only=False):
    o='<div style="display:flex;gap:4px">'
    for l,c,sym in [("G",G,"●"),("Y",Y,"▲"),("R",R,"■")]:
        on=(l==sel)
        bd=f"1.5px solid {c}" if on else f"1px solid {LINE}"
        o+=f'<span style="width:44px;height:44px;box-sizing:border-box;border-radius:8px;border:{bd};background:{SURF};display:flex;flex-direction:column;align-items:center;justify-content:center;font-size:11px;font-weight:700;color:{TXT}"><span style="color:{c};font-size:13px">{sym}</span>{l}</span>'
    return o+'</div>'
b+=f'<div style="display:flex;justify-content:flex-end;gap:84px;padding-right:8px;font-size:11px;font-weight:700;color:{MUT}"><span>Before</span><span>After</span></div>\n'
for j,bf,af in [("Back","G","Y"),("Knee","G","G"),("Shoulder","G","G"),("Ankle","G","G")]:
    b+=card(f'<div style="display:flex;align-items:center;justify-content:space-between"><div style="font-size:16px;font-weight:800">{j}</div><div style="display:flex;gap:12px">{zone(bf)}{zone(af)}</div></div>',pad=8)
b+=card(f'<div style="display:flex;gap:10px;align-items:flex-start"><span style="color:{Y};font-size:16px">▲</span><div><div style="font-size:14px;font-weight:700">Yellow logged for back</div><div style="font-size:13px;color:{MUT};margin-top:2px">Reduce resistance or range, slow down, use the substitute movement, or drop a set. Red means stop the movement.</div></div></div>',pad=12)
b+='<div style="margin-top:auto;padding-bottom:10px">'+gbtn("Save","Track.dc.html","check",50)+'</div>\n</div>\n'+tabs("Progress")+FOOT
files["Joint.dc.html"]=b
# ---- Rewards
b=head("Cindrix Fitness - Rank and rewards")+title("Rank and Rewards","trophy",back="Main.dc.html",sub="Sample data for layout only")
b+='<div style="flex:1;padding:0 16px;display:flex;flex-direction:column;gap:9px;min-height:0">\n'
b+=card(f'<div style="display:flex;align-items:center;gap:12px"><div style="width:56px;height:56px;border-radius:50%;border:2px solid {ACC};display:flex;align-items:center;justify-content:center;color:{ACC};box-shadow:0 0 14px rgba(43,140,255,0.35)">{ic("shield",30)}</div><div style="flex:1"><div style="font-size:20px;font-weight:800">Operator</div><div style="font-size:12px;color:{MUT}">Level 3 · next rank Specialist at level 5</div><div style="margin-top:6px">{bar(64)}</div><div style="font-size:11px;color:{MUT};margin-top:3px">640 / 1,000 XP</div></div></div>',pad=12)
b+=f'<div style="display:flex;gap:8px"><div style="flex:1;background:{SURF};border:1px solid {LINE};border-radius:12px;padding:10px"><div style="font-size:20px;font-weight:800">4 weeks</div><div style="font-size:11px;color:{MUT}">Training streak</div></div><div style="flex:1;background:{SURF};border:1px solid {LINE};border-radius:12px;padding:10px"><div style="font-size:20px;font-weight:800">7</div><div style="font-size:11px;color:{MUT}">Band-ups earned</div></div></div>\n'
bd=f'<div style="font-size:15px;font-weight:700;margin-bottom:8px">Badges</div><div style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px">'
for icn,n,on in [("check","First workout",True),("bolt","First band-up",True),("star","10 sessions",True),("shield","4-week check",True),("trophy","Clean week",False),("ball","5 boxing rounds",False),("fork","Protein week",False),("heart","8-week check",False),("lock","12-week finish",False)]:
    col=ACC if on else MUT
    op="1" if on else "0.7"
    bd+=f'<div style="text-align:center;opacity:{op}"><div style="width:48px;height:48px;margin:0 auto;border-radius:50%;border:{"2px solid "+ACC if on else "1px dashed "+MUT};display:flex;align-items:center;justify-content:center;color:{col}">{ic(icn,24)}</div><div style="font-size:11px;font-weight:600;margin-top:4px;color:{TXT if on else MUT}">{n}</div></div>'
b+=card(bd+'</div>',pad=12)
b+=card(f'<div style="font-size:13px;font-weight:700;margin-bottom:4px">How XP works</div><div style="font-size:12px;color:{MUT};line-height:1.45">Workout +100 · Band-up +50 · Weekly quests +150 · Easy or recovery week +100. Streaks count weeks, not days, so rest days never break them. Pain and yellow-zone logs earn XP when you adjust, never for training through them.</div>',pad=12)
b+='</div>\n'+tabs("Today")+FOOT
files["Rewards.dc.html"]=b
# ---- Library
b=head("Cindrix Fitness - Exercise Library")+title("Library","book")
b+=f'<div style="padding:0 16px 8px;flex:none"><label style="display:flex;align-items:center;gap:8px;height:44px;border:1px solid {LINE};border-radius:12px;background:{SURF};padding:0 12px;color:{MUT}">{ic("search",18)}<input aria-label="Search exercises" placeholder="Search exercises" style="flex:1;background:none;border:0;color:{TXT};font-size:14px;outline:none"></label></div>\n'
ch=f'<div style="display:flex;gap:6px;padding:0 16px 10px;flex:none;overflow:hidden">'
for i,l in enumerate(["All","Push","Pull","Legs","Core","Boxing"]):
    if i==0: ch+=f'<span style="height:32px;display:flex;align-items:center;padding:0 14px;border-radius:999px;background:linear-gradient(180deg,{B1},{B2});font-size:13px;font-weight:700;color:#fff">{l}</span>'
    else: ch+=f'<span style="height:32px;display:flex;align-items:center;padding:0 14px;border-radius:999px;border:1px solid {LINE};font-size:13px;font-weight:600;color:{MUT}">{l}</span>'
b+=ch+'</div>\n<div style="flex:1;padding:0 16px;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));grid-auto-rows:min-content;gap:8px;min-height:0;overflow:hidden">\n'
for k,n,m in [("chest_press","Chest press","Push · middle D-ring"),("seated_row","Seated row","Pull · drum throne"),("lat_pulldown","Lat pulldown","Pull · top D-ring"),("triceps_pressdown","Triceps pressdown","Push · top D-ring"),("band_bar_squat","Band-bar squat","Legs · stand on band"),("pull_through","Pull-through","Legs · bottom D-ring"),("pallof_press","Pallof press","Core · middle D-ring"),("overhead_press","Overhead press","Push · band bar")]:
    b+=f'<a href="Modal.dc.html" style="display:block;background:{SURF};border:1px solid {LINE};border-radius:12px;overflow:hidden"><div style="height:84px;background:#fff;overflow:hidden"><img src="{IMG[k]}" alt="" style="display:block;width:100%;height:84px;object-fit:cover"></div><div style="padding:7px 10px"><div style="font-size:13px;font-weight:700">{n}</div><div style="font-size:11px;color:{MUT};margin-top:1px">{m}</div></div></a>\n'
b+='</div>\n'+tabs("Library")+FOOT
files["Library.dc.html"]=b
import os
for f in os.listdir("/home/claude/cfit/project"):
    if f.endswith(".dc.html") and f not in files: os.remove("/home/claude/cfit/project/"+f)
for k,v in files.items(): open("/home/claude/cfit/project/"+k,"w").write(v)
order=["Main.dc.html","Rewards.dc.html","Program.dc.html","Workout.dc.html","Modal.dc.html","Exercise.dc.html","Progress.dc.html","Track.dc.html","Joint.dc.html","Library.dc.html"]
ttl={"Main.dc.html":"Today","Rewards.dc.html":"Rank and rewards","Program.dc.html":"Program","Workout.dc.html":"Monday workout","Modal.dc.html":"Exercise how-to modal","Exercise.dc.html":"Active set","Progress.dc.html":"Progress","Track.dc.html":"Trackers","Joint.dc.html":"Joint and pain","Library.dc.html":"Exercise library"}
boards={k:{"x":i*470,"y":0,"w":390,"h":844,"title":ttl[k],"is_interactive":True} for i,k in enumerate(order)}
idx={"v":3,"createdOnFiles":{"v":1,"at":"2026-11-20T22:30:00Z"},"title":"Cindrix Fitness - App Mockups","launch":{"view":"canvas"},"pages":[],"boards":boards,"order":order,"notes":{"t":{"x":0,"y":-260,"text":"Cindrix Fitness","kind":"title1","maxW":4620}},"designSystems":[]}
open("/home/claude/cfit/project/canvas.json","w").write(json.dumps(idx,indent=1))
print("ok",len(files))
