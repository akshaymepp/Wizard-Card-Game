package com.akshay.wizardgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Night = Color(0xFF080D20)
private val Panel = Color(0xFF11182E)
private val Gold = Color(0xFFF4C66A)
private val Purple = Color(0xFF6D3CC8)
private val Red = Color(0xFFFF555D)
private val Green = Color(0xFF35D69A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WizardApp() }
    }
}

data class Player(val name: String, val isAI: Boolean = false, val bid: Int? = null, val won: Int = 0, val score: Int = 0)

@Composable fun WizardApp() {
    var screen by remember { mutableStateOf(0) }
    var players by remember { mutableStateOf(listOf(Player("Akshay"), Player("Rahul"), Player("Nithin"), Player("Arun"))) }
    var round by remember { mutableIntStateOf(1) }
    var bid by remember { mutableIntStateOf(0) }
    var current by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(20) }
    var room by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("192.168.1.10:8080") }
    val cardsEach = round
    val maxRounds = (60 / players.size).coerceAtLeast(1)
    val totalBids = players.mapNotNull { it.bid }.sum()
    val invalid = players.all { it.bid != null } && totalBids == cardsEach

    LaunchedEffect(screen, current) {
        if (screen == 1) {
            seconds = 20
            while (seconds > 0 && screen == 1) { delay(1000); seconds-- }
            if (seconds == 0 && players[current].bid == null) {
                players = players.toMutableList().also { it[current] = it[current].copy(bid = 0) }
                current = (current + 1) % players.size
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Night, surface = Panel, primary = Gold)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101A3B), Night, Color(0xFF160E2B))))) {
            when (screen) {
                0 -> MainGame(players, round, maxRounds, cardsEach, onBid = { screen = 1 }, onSettings = { screen = 3 }, onScore = { screen = 2 })
                1 -> BidScreen(players[current].name, round, cardsEach, bid, { bid = (bid + 1).coerceAtMost(cardsEach) }, { bid = (bid - 1).coerceAtLeast(0) }, seconds, totalBids, invalid, { value ->
                    players = players.toMutableList().also { it[current] = it[current].copy(bid = value) }
                    if (current < players.lastIndex) current++ else screen = 2
                    bid = 0
                })
                2 -> ResultsScreen(players, round, onNext = {
                    players = players.map { it.copy(bid = null, won = 0) }
                    round = if (round >= maxRounds) 1 else round + 1
                    current = 0; screen = 0
                }, onHome = { screen = 0 })
                else -> SettingsScreen(players, aiCount, aiDifficulty, gameMode, host, room, { host = it }, { room = it }, { aiDifficulty = it }, { gameMode = it },
                    { updated -> players = updated },
                    { count ->
                        val humans = players.filterNot { it.isAI }
                        val bots = (1..count).map { Player("Computer $it", isAI = true) }
                        players = humans + bots
                        aiCount = count
                    }, { screen = 0 })
            }
        }
    }
}

@Composable fun GoldButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = Purple, disabledContainerColor = Color.DarkGray), shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Gold)) {
        Text(text, color = if (enabled) Gold else Color.LightGray, fontWeight = FontWeight.Bold)
    }
}
@Composable fun PanelBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(Panel.copy(alpha=.94f), RoundedCornerShape(20.dp)).border(1.dp, Gold.copy(alpha=.65f), RoundedCornerShape(20.dp)).padding(14.dp), content = content)
}
@Composable fun Header(title: String, subtitle: String) {
    Text("✦  W I Z A R D  ✦", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Text(title, color = Gold, fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
    Text(subtitle, color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom=14.dp))
}
@Composable fun MainGame(players: List<Player>, round: Int, maxRounds: Int, cards: Int, onBid: () -> Unit, onSettings: () -> Unit, onScore: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("⚙", color = Gold, fontSize = 24.sp, modifier = Modifier.clickable { onSettings() })
            Text("🏆", color = Gold, fontSize = 24.sp, modifier = Modifier.clickable { onScore() })
        }
        Header("Round $round / $maxRounds", "$cards cards per player  •  ${players.size} players")
        PanelBox(Modifier.fillMaxWidth()) {
            Text("THE ENCHANTED TABLE", color = Gold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            players.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { p -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("◉", color = Gold, fontSize = 24.sp)
                        Text(p.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Bid: ${p.bid?.toString() ?: "—"}   Won: ${p.won}", color = Gold, fontSize = 12.sp)
                    } }
                }
                Spacer(Modifier.height(12.dp))
            }
            Text("🧙  WIZARD     🃏  JESTER", color = Gold, fontSize = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("Your hand  •  $cards cards", color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(cards.coerceAtMost(7)) { i ->
                    Box(Modifier.padding(3.dp).size(width=42.dp, height=70.dp).background(Color(0xFFF2E8D4), RoundedCornerShape(7.dp)).border(1.dp, Gold, RoundedCornerShape(7.dp)), contentAlignment=Alignment.Center) {
                        Text(listOf("J","4","8","Q","W","3","K")[i], color= if (i==0) Color.Red else Color(0xFF182B59), fontWeight=FontWeight.Black, fontSize=20.sp)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        GoldButton("✦  PLACE BID  ✦", Modifier.fillMaxWidth().height(58.dp), onClick = onBid)
        Text("Game settings     •     Scoreboard", color=Color.LightGray, modifier=Modifier.padding(12.dp))
    }
}
@Composable fun BidScreen(name: String, round: Int, cards: Int, bid: Int, plus: () -> Unit, minus: () -> Unit, seconds: Int, total: Int, invalid: Boolean, confirm: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
        Header("PLACE YOUR BID", "Round $round  /  $cards cards")
        PanelBox(Modifier.fillMaxWidth()) {
            Text("⌛  $seconds s", color=if(seconds<=5) Red else Gold, fontSize=34.sp, fontWeight=FontWeight.Black, modifier=Modifier.fillMaxWidth(), textAlign=TextAlign.Center)
            Text("YOUR TURN  •  $name", color=Color.White, fontSize=18.sp, modifier=Modifier.fillMaxWidth(), textAlign=TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.Center, modifier=Modifier.fillMaxWidth()) {
                IconButton(onClick=minus, modifier=Modifier.size(58.dp).border(1.dp, Gold, CircleShape)) { Icon(Icons.Default.Remove, null, tint=Gold) }
                Text("$bid", color=Gold, fontSize=70.sp, fontWeight=FontWeight.Black, modifier=Modifier.padding(horizontal=28.dp))
                IconButton(onClick=plus, modifier=Modifier.size(58.dp).border(1.dp, Gold, CircleShape)) { Icon(Icons.Default.Add, null, tint=Gold) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly) {
                (0..cards).forEach { v -> Text("$v", color=if(v==bid) Gold else Color.White, fontSize=20.sp, fontWeight=FontWeight.Bold, modifier=Modifier.clickable { if(v>=0) { /* quick-select visual; +/- remains active */ } }.padding(8.dp)) }
            }
            Spacer(Modifier.height(14.dp))
            Text("TOTAL BIDS SO FAR: $total     •     ROUND CARDS: $cards", color=Color.LightGray, fontSize=12.sp)
            if (invalid) Text("⚠ Total bids cannot equal $cards", color=Red, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            GoldButton("CONFIRM BID", Modifier.fillMaxWidth(), enabled = !invalid && seconds > 0) { confirm(bid) }
        }
    }
}
@Composable fun ResultsScreen(players: List<Player>, round: Int, onNext: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header("ROUND $round COMPLETE", "Round summary")
        PanelBox(Modifier.weight(1f).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                listOf("PLAYER","BID","WON","POINTS").forEach { Text(it, color=Gold, fontSize=11.sp, fontWeight=FontWeight.Bold) }
            }
            LazyColumn {
                items(players.size) { i ->
                    val p=players[i]
                    Row(Modifier.fillMaxWidth().padding(vertical=12.dp), horizontalArrangement=Arrangement.SpaceBetween) {
                        Text((if (p.isAI) "🤖 " else "") + p.name, color=Color.White, modifier=Modifier.weight(1f))
                        Text("${p.bid ?: 0}", color=Color.White, modifier=Modifier.weight(.4f))
                        Text("${p.won}", color=Color.White, modifier=Modifier.weight(.4f))
                        Text(if(p.bid==p.won) "+${20+p.won*10}" else "-10", color=if(p.bid==p.won) Green else Red, modifier=Modifier.weight(.5f))
                    }
                    HorizontalDivider(color=Color.DarkGray)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("TOTAL SCORE", color=Gold, fontWeight=FontWeight.Bold)
            players.forEach { Text("${it.name}     ${it.score}", color=Color.White) }
        }
        Spacer(Modifier.height(12.dp))
        GoldButton("▶  NEXT ROUND", Modifier.fillMaxWidth(), onClick=onNext)
        TextButton(onClick=onHome, modifier=Modifier.align(Alignment.CenterHorizontally)) { Text("Back to game", color=Gold) }
    }
}
@Composable fun SettingsScreen(
    players: List<Player>, aiCount: Int, host: String, room: String,
    setHost: (String)->Unit, setRoom: (String)->Unit,
    update: (List<Player>)->Unit, setAiCount: (Int)->Unit, back: ()->Unit
) {
    val humans = players.filterNot { it.isAI }
    val maxAi = (10 - humans.size).coerceAtLeast(0)
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement=Arrangement.Center) {
        Header("PRIVATE MULTIPLAYER", "Configure players before starting the game")
        PanelBox(Modifier.fillMaxWidth()) {
            Text("GAME MODE", color=Gold, fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("Offline", "Private VPN").forEach { mode ->
                    FilterChip(selected = gameMode == mode, onClick = { setGameMode(mode) }, label = { Text(mode) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Purple, selectedLabelColor = Gold))
                }
            }
            Text(if (gameMode == "Offline") "Play without internet or VPN. Add computer opponents below." else "Connect to your private OpenVPN network to play with friends.", color=Color.LightGray, fontSize=11.sp)
            Text("HUMAN PLAYERS: ${humans.size}", color=Gold, fontWeight=FontWeight.Bold)
            Text("COMPUTER / AI PLAYERS", color=Gold, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=14.dp))
            Text("Choose how many computer opponents the admin wants to add. Total players cannot exceed 10.", color=Color.LightGray, fontSize=12.sp)
            Row(Modifier.fillMaxWidth().padding(vertical=10.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
                IconButton(onClick={ setAiCount((aiCount-1).coerceAtLeast(0)) }, enabled=aiCount>0) {
                    Icon(Icons.Default.Remove, null, tint=Gold)
                }
                Text("$aiCount AI PLAYERS", color=Gold, fontSize=22.sp, fontWeight=FontWeight.Black)
                IconButton(onClick={ setAiCount((aiCount+1).coerceAtMost(maxAi)) }, enabled=aiCount<maxAi) {
                    Icon(Icons.Default.Add, null, tint=Gold)
                }
            }
            Text("Total players: ${humans.size + aiCount} / 10", color=Color.White, fontWeight=FontWeight.Bold)
            Text("AI DIFFICULTY", color=Gold, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("Easy", "Medium", "Hard").forEach { level ->
                    FilterChip(selected = aiDifficulty == level, onClick = { setAiDifficulty(level) }, label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Purple, selectedLabelColor = Gold))
                }
            }
            Text("Medium is the default: balanced bids and card choices (AI strategy implementation pending).", color=Color.LightGray, fontSize=11.sp)
            LinearProgressIndicator(progress = { ((humans.size + aiCount) / 10f).coerceIn(0f,1f) }, modifier=Modifier.fillMaxWidth(), color=Gold, trackColor=Color.DarkGray)
            Spacer(Modifier.height(12.dp))
            Text("PLAYER NAMES", color=Gold, fontWeight=FontWeight.Bold)
            humans.forEachIndexed { i,p ->
                OutlinedTextField(p.name, { v -> update(players.toMutableList().also { val idx=it.indexOfFirst { x -> !x.isAI && x.name==p.name }; if(idx>=0) it[idx]=p.copy(name=v) }) }, modifier=Modifier.fillMaxWidth(), singleLine=true, label={ Text("Human player ${i+1}") })
            }
            (1..aiCount).forEach { i ->
                OutlinedTextField("Computer $i", {}, modifier=Modifier.fillMaxWidth(), enabled=false, singleLine=true, label={ Text("AI opponent") })
            }
            Spacer(Modifier.height(12.dp))
            Text("VPN SERVER", color=Gold, fontWeight=FontWeight.Bold)
            OutlinedTextField(host, setHost, modifier=Modifier.fillMaxWidth(), singleLine=true, label={ Text("Server VPN IP:port") })
            OutlinedTextField(room, setRoom, modifier=Modifier.fillMaxWidth(), singleLine=true, label={ Text("Room code") })
            Text("Friends must connect to your OpenVPN network before joining. AI opponent gameplay logic and live VPN networking are still pending implementation.", color=Color.LightGray, fontSize=11.sp)
        }
        Spacer(Modifier.height(14.dp))
        GoldButton("SAVE & RETURN", Modifier.fillMaxWidth(), onClick=back)
    }
}
