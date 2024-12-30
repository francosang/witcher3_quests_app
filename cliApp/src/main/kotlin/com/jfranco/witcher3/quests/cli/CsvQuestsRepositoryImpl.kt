package com.jfranco.witcher3.quests.cli

import com.jfranco.w3.quests.shared.ExtraDetail
import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestsRepository
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.fillNulls
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.prev
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.io.readCSV
import java.io.InputStream

class CsvQuestsRepositoryImpl(
    inputStream: InputStream
) : QuestsRepository {

    private data class QuestFlat(
        val location: String,
        val quest: String,
        val isCompleted: Boolean,
        val suggested: Level,
        val url: String,
        val order: Order,
        val branch: String?,
        val detail: ExtraDetail?,
    )

    private data class QuestKey(
        val location: String,
        val quest: String,
        val isCompleted: Boolean,
        val suggested: Level,
        val url: String,
        val branch: String?,
    )

    private val questLinks = mapOf(
        "Kaer Morhen (1)" to "https://witcher.fandom.com/wiki/Kaer_Morhen_(quest)?so=search",


        "Lilac and Gooseberries Part 1 (1)" to "https://witcher.fandom.com/wiki/Lilac_and_Gooseberries?so=search",


        "Twisted Firestarter (1)" to "https://witcher.fandom.com/wiki/Twisted_Firestarter?so=search",
        "A Frying Pan, Spick and Span (1)" to "https://witcher.fandom.com/wiki/A_Frying_Pan,_Spick_and_Span?so=search",


        "Precious Cargo (1)" to "https://witcher.fandom.com/wiki/Precious_Cargo?so=search",
        "Missing in Action (1)" to "https://witcher.fandom.com/wiki/Missing_in_Action?so=search",

        "Devil by the Well (2)" to "https://witcher.fandom.com/wiki/Devil_by_the_Well?so=search",
        "On Death's Bed (2)" to "https://witcher.fandom.com/wiki/On_Death's_Bed?so=search",

        "The Beast of White Orchard (3)" to "https://witcher.fandom.com/wiki/The_Beast_of_White_Orchard?so=search",


        "Lilac and Gooseberries Part 2 (1)" to "https://witcher.fandom.com/wiki/Lilac_and_Gooseberries?so=search",
        "The Incident at White Orchard (2)" to "https://witcher.fandom.com/wiki/The_Incident_at_White_Orchard?so=search",


        "Dirty Funds (2)" to "https://witcher.fandom.com/wiki/Dirty_Funds?so=search",
        "Deserter Gold (3)" to "https://witcher.fandom.com/wiki/Deserter_Gold?so=search",
        "Temerian Valuables (4)" to "https://witcher.fandom.com/wiki/Temerian_Valuables?so=search",
        "Viper Silver Sword (Basic) (6)" to "https://witcher.fandom.com/wiki/Viper_School_Gear?so=search",
        "Viper Steel Sword (Basic) (6)" to "https://witcher.fandom.com/wiki/Viper_School_Gear?so=search",
        "Gwent: Collect 'em All! (1)" to "https://witcher.fandom.com/wiki/Collect_'Em_All?so=search",


        "Imperial Audience (2)" to "https://witcher.fandom.com/wiki/Imperial_Audience?so=search",


        "Gwent: Collect 'em All! (1)" to "https://witcher.fandom.com/wiki/Collect_'Em_All?so=search",
        "The Nilfgaardian Connection (5)" to "https://witcher.fandom.com/wiki/The_Nilfgaardian_Connection?so=search",


        "At the Mercy of Strangers" to "https://witcher.fandom.com/wiki/At_the_Mercy_of_Strangers?so=search",

        "Man's Best Friend" to "https://witcher.fandom.com/wiki/Man%27s_Best_Friend",
        "Deadly Crossing (II)" to "https://witcher.fandom.com/wiki/Deadly_Crossing?so=search",


        "Caravan Attack" to "https://witcher.fandom.com/wiki/Caravan_Attack?so=search",
        "Harassing a Troll" to "https://witcher.fandom.com/wiki/Harassing_a_Troll?so=search",
        "Looters (I)" to "https://witcher.fandom.com/wiki/Looters?so=search",
        "Face Me if You Dare (I)" to "https://witcher.fandom.com/wiki/Face_Me_if_You_Dare!?so=search",
        "Bloody Baron (6)" to "https://witcher.fandom.com/wiki/Bloody_Baron_(quest)?so=search",


        "Ciri's Story: The King of the Wolves (5)" to "https://witcher.fandom.com/wiki/Ciri%27s_Story:_The_King_of_the_Wolves?so=search",
        "Family Matters (Part 1) (5)" to "https://witcher.fandom.com/wiki/Family_Matters?so=search",


        "Ciri's Room (5)" to "https://witcher.fandom.com/wiki/Ciri's_Room?so=search",


        "The Mysterious Passenger (1)" to "https://witcher.fandom.com/wiki/A_Mysterious_Passenger",


        "Deadly Crossing (I)" to "https://witcher.fandom.com/wiki/Deadly_Crossing?so=search",
        "Highway Robbery" to "https://witcher.fandom.com/wiki/Highway_Robbery?so=search",
        "Highwayman's Cache" to "https://witcher.fandom.com/wiki/Highwayman's_Cache?so=search",
        "A Princess in Distress (5)" to "https://witcher.fandom.com/wiki/A_Princess_in_Distress?so=search",
        "Crow's Perch Fight" to "https://www.tivaprojects.com/witcher3map/v/index.html#4/109.41/92.31/m=101.625,74.625",
        "Thou Shalt Not Pass (7)" to "https://witcher.fandom.com/wiki/Thou_Shalt_Not_Pass?so=search",
        "Deadly Crossing (III)" to "https://witcher.fandom.com/wiki/Deadly_Crossing?so=search",
        "Death By Fire (10)" to "https://witcher.fandom.com/wiki/Death_By_Fire?so=search",
        "Lynch Mob (7)" to "https://witcher.fandom.com/wiki/Lynch_Mob?so=search",
        "The Most Truest of Basilisks" to "https://witcher.fandom.com/wiki/The_Most_Truest_of_Basilisks?so=search",
        "Ciri's Story: The Race (5)" to "https://witcher.fandom.com/wiki/Ciri%27s_Story:_The_Race?so=search",
        "Hunting a Witch (5)" to "https://witcher.fandom.com/wiki/Hunting_a_Witch?so=search",
        "Wandering in the Dark (6)" to "https://witcher.fandom.com/wiki/Wandering_in_the_Dark?so=search",

        "Magic Lamp (6)" to "https://witcher.fandom.com/wiki/Magic_Lamp_(quest)?so=search",
        "Races: Crow's Perch (1)" to "https://witcher.fandom.com/wiki/Races:_Crow%27s_Perch?so=search",
        "Hazardous Goods (5)" to "https://witcher.fandom.com/wiki/Hazardous_Goods?so=search",

        "An Invitation from Keira Metz (6)" to "https://witcher.fandom.com/wiki/An_Invitation_from_Keira_Metz?so=search",
        "A Towerful of Mice (6)" to "https://witcher.fandom.com/wiki/A_Towerful_of_Mice?so=search",


        "A Favor for a Friend (6)" to "https://witcher.fandom.com/wiki/A_Favor_for_a_Friend?so=search",

        "For the Advancement of Learning (8)" to "https://witcher.fandom.com/wiki/For_the_Advancement_of_Learning?so=search",

        "Looters (II)" to "https://witcher.fandom.com/wiki/Looters?so=search",
        "Defender of the Faith (I) (10)" to "https://witcher.fandom.com/wiki/Defender_of_the_Faith?so=search",


        "Defender of the Faith (II) (10)" to "https://witcher.fandom.com/wiki/Defender_of_the_Faith?so=search",

        "A Dog's Life (5)" to "https://witcher.fandom.com/wiki/A_Dog's_Life?so=search",
        "The Fall of the House of Reardon (6)" to "https://witcher.fandom.com/wiki/The_Fall_of_the_House_of_Reardon?so=search",


        "Ghosts of the Past (6)" to "https://witcher.fandom.com/wiki/Ghosts_of_the_Past?so=search",


        "In the Eternal Fire's Shadow (15)" to "https://witcher.fandom.com/wiki/In_the_Eternal_Fire%27s_Shadow",


        "Gwent: Velen Players (1)" to "https://witcher.fandom.com/wiki/Gwent:_Velen_Players?so=search",
        "Looters (III)" to "https://witcher.fandom.com/wiki/Looters?so=search",
        "Bitter Harvest (9)" to "https://witcher.fandom.com/wiki/Bitter_Harvest?so=search",

        "Fake Papers (1)" to "https://witcher.fandom.com/wiki/Fake_Papers?so=search",
        "Ladies of the Wood (6)" to "https://witcher.fandom.com/wiki/Ladies_of_the_Wood?so=search",

        "The Whispering Hillock (5)" to "https://witcher.fandom.com/wiki/The_Whispering_Hillock?so=search",

        "Ciri's Story: Fleeing the Bog (5)" to "https://witcher.fandom.com/wiki/Ciri%27s_Story:_Fleeing_the_Bog?so=search",
        "Family Matters (Part 2) (5)" to "https://witcher.fandom.com/wiki/Family_Matters?so=search",

        "Ciri's Story: Out of the Shadows (5)" to "https://witcher.fandom.com/wiki/Ciri%27s_Story:_Out_of_the_Shadows?so=search",
        "In Ciri's Footsteps: Velen" to "https://witcher.fandom.com/wiki/In_Ciri%27s_Footsteps#:~:text=In%20Ciri's%20Footsteps%20is%20a,areas%20to%20complete%20this%20quest.",
        "Return to Crookback Bog (9)" to "https://witcher.fandom.com/wiki/Return_to_Crookback_Bog?so=search",
        "Saving Farmer's Daughter From Soldiers (I)" to "https://www.tivaprojects.com/witcher3map/v/index.html#4/109.41/92.31/m=101.625,74.625",
        "Saving Farmer's Daughter From Soldiers (II)" to "https://www.tivaprojects.com/witcher3map/v/index.html#4/109.41/92.31/m=101.625,74.625",


        "The Truth is in the Stars (1)" to "https://witcher.fandom.com/wiki/The_Truth_is_in_the_Stars?so=search",
        "Funeral Pyres (3)" to "https://witcher.fandom.com/wiki/Funeral_Pyres?so=search",
        "Fool's Gold (6)" to "https://witcher.fandom.com/wiki/Fools'_Gold?so=search",


        "Wild at Heart (7)" to "https://witcher.fandom.com/wiki/Wild_at_Heart?so=search",
        "Forefathers' Eve (7)" to "https://witcher.fandom.com/wiki/Forefathers'_Eve?so=search",
        "A Greedy God (7)" to "https://witcher.fandom.com/wiki/A_Greedy_God?so=search",
        "Last Rites (9)" to "https://witcher.fandom.com/wiki/Last_Rites?so=search",
        "Love's Cruel Snares (10)" to "https://witcher.fandom.com/wiki/Love's_Cruel_Snares?so=search",

        "Witcher Wannabe (10)" to "https://witcher.fandom.com/wiki/Witcher_Wannabe?so=search",
        "Blood Ties (12)" to "https://witcher.fandom.com/wiki/Blood_Ties?so=search",
        "The Volunteer (13)" to "https://witcher.fandom.com/wiki/The_Volunteer?so=search",
        "Master Armorers (24)" to "https://witcher.fandom.com/wiki/Master_Armorers?so=search",
        "The Griffin from the Highlands (24)" to "https://witcher.fandom.com/wiki/Contract:_The_Griffin_from_the_Highlands?so=search",
        "Components for an Armorer (24)" to "https://thewitcher3.wiki.fextralife.com/Contract:+Components+for+an+Armorer",
        "The Beast of Honorton (25)" to "https://witcher.fandom.com/wiki/Contract:_The_Beast_of_Honorton",
        "Where the Cat and Wolf Play (25)" to "https://witcher.fandom.com/wiki/Where_the_Cat_and_Wolf_Play...?so=search",


        "Take What You Want (25)" to "https://witcher.fandom.com/wiki/Take_What_You_Want?so=search",
        "Woodland Beast (6)" to "https://witcher.fandom.com/wiki/Contract:_Woodland_Beast?so=search",
        "Patrol Gone Missing (7)" to "https://witcher.fandom.com/wiki/Contract:_Patrol_Gone_Missing?so=search",

        "Shrieker (8)" to "https://witcher.fandom.com/wiki/Contract:_Shrieker",
        "Jenny O' the Woods (10)" to "https://witcher.fandom.com/wiki/Contract:_Jenny_o%27_the_Woods",
        "The Merry Widow (10)" to "https://witcher.fandom.com/wiki/Contract:_The_Merry_Widow?so=search",
        "Swamp Thing (12)" to "https://witcher.fandom.com/wiki/Contract:_Swamp_Thing?so=search",
        "Mysterious Tracks (20)" to "https://witcher.fandom.com/wiki/Contract:_Mysterious_Tracks?so=search",
        "The Mystery of the Byways Murders (22)" to "https://witcher.fandom.com/wiki/Contract:_The_Mystery_of_the_Byways_Murders?so=search",
        "Phantom of the Trade Route (23)" to "https://witcher.fandom.com/wiki/Contract:_Phantom_of_the_Trade_Route?so=search",
        "Missing Brother (33)" to "https://witcher.fandom.com/wiki/Contract:_Missing_Brother?so=search",
        "Queen Zuleyka's Treasure (1)" to "https://witcher.fandom.com/wiki/Queen_Zuleyka's_Treasure?so=search",
        "Lost Goods (1)" to "https://witcher.fandom.com/wiki/Lost_Goods?so=search",
        "Out of the Frying Pan, Into the Fire (1)" to "https://witcher.fandom.com/wiki/Out_of_the_Frying_Pan,_into_the_Fire?so=search",
        "Sunken Treasure (4)" to "https://witcher.fandom.com/wiki/Sunken_Treasure?so=search",
        "An Unfortunate Turn of Events (4)" to "https://witcher.fandom.com/wiki/An_Unfortunate_Turn_of_Events?so=search",
        "Sunken Chest (4)" to "https://witcher.fandom.com/wiki/Sunken_Chest?so=search",
        "Hidden from the World (7)" to "https://witcher.fandom.com/wiki/Hidden_from_the_World?so=search",
        "The Dead Have No Defense (9)" to "https://witcher.fandom.com/wiki/The_Dead_Have_No_Defense?so=search",
        "The Things Men Do For Coin... (12)" to "https://witcher.fandom.com/wiki/The_Things_Men_Do_For_Coin...?so=search",
        "Don't Play With Gods (15)" to "https://witcher.fandom.com/wiki/Don't_Play_with_the_Gods?so=search",

        "Costly Mistake (18)" to "https://witcher.fandom.com/wiki/A_Costly_Mistake",
        "Blood Gold (18)" to "https://witcher.fandom.com/wiki/Blood_Gold?so=search",
        "Tough Luck (18)" to "https://witcher.fandom.com/wiki/Tough_Luck?so=search",
        "A Plea Ignored (28)" to "https://witcher.fandom.com/wiki/A_Plea_Ignored?so=search",
        "Dowry (32)" to "https://witcher.fandom.com/wiki/Dowry?so=search",
        "Griffin School Gear (Basic) (11)" to "https://witcher.fandom.com/wiki/Griffin_School_Gear?so=search",

        "Griffin School Part 1 (18)" to "https://witcher.fandom.com/wiki/Griffin_School_Gear?so=search",
        "Griffin School Part 2 (18)" to "https://witcher.fandom.com/wiki/Griffin_School_Gear?so=search",
        "Forgotten Wolf School Gear (Basic) (20)" to "https://witcher.fandom.com/wiki/In_the_Eternal_Fire's_Shadow?so=search",
        "Wolf School Part 1 (21)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Wolf School Part 5 (34)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Cat School Part 2 (23)" to "https://witcher.fandom.com/wiki/Cat_School_Gear?so=search",
        "Cat School Part 3 (29)" to "https://witcher.fandom.com/wiki/Cat_School_Gear?so=search",
        "Gwent: Collect 'em All! (1)" to "https://witcher.fandom.com/wiki/Collect_'Em_All?so=search",
        "Gwent: Playing Innkeeps (1)" to "https://witcher.fandom.com/wiki/Gwent:_Playing_Innkeeps?so=search",
        "Gwent: Old Pals (5)" to "https://witcher.fandom.com/wiki/Gwent:_Old_Pals?so=search",
        "Fists of Fury: Velen (11)" to "https://witcher.fandom.com/wiki/Fists_of_Fury:_Velen?so=search",


        "Racists of Novigrad (I)" to "https://witcher.fandom.com/wiki/Racists_of_Novigrad?so=search",
        "A Walk on the Waterfront (12)" to "https://witcher.fandom.com/wiki/A_Walk_on_the_Waterfront?so=search",

        "Warehouse of Woe (13)" to "https://witcher.fandom.com/wiki/Warehouse_of_Woe?so=search",
        "Strumpet in Distress" to "https://witcher.fandom.com/wiki/Strumpet_in_Distress?so=search",
        "Racists of Novigrad (II)" to "https://witcher.fandom.com/wiki/Racists_of_Novigrad?so=search",
        "Karmic Justice" to "https://witcher.fandom.com/wiki/Karmic_Justice?so=search",

        "Face Me if You Dare (II)" to "https://witcher.fandom.com/wiki/Face_Me_if_You_Dare!?so=search",
        "Drunken Rabble" to "https://witcher.fandom.com/wiki/Drunken_Rabble?so=search",
        "Pyres of Novigrad (10)" to "https://witcher.fandom.com/wiki/Pyres_of_Novigrad?so=search",


        "Suspicious Shakedown (10)" to "https://witcher.fandom.com/wiki/Suspicious_Shakedown",
        "Witch Hunter Raids (I)" to "https://witcher.fandom.com/wiki/Witch_Hunter_Raids?so=search",


        "The Flame of Hatred (I)" to "https://witcher.fandom.com/wiki/The_Flame_of_Hatred?so=search",
        "Never Trust Children (I)" to "https://witcher.fandom.com/wiki/Never_Trust_Children?so=search",
        "Hidden Messages of the Nilfgaardian Kind (8)" to "https://witcher.fandom.com/wiki/Hidden_Messages_of_the_Nilfgaardian_Kind",
        "The Flame of Hatred (II)" to "https://witcher.fandom.com/wiki/The_Flame_of_Hatred?so=search",
        "Novigrad Dreaming (7)" to "https://witcher.fandom.com/wiki/Novigrad_Dreaming?so=search",

        "Haunted House (7)" to "https://witcher.fandom.com/wiki/Haunted_House_(quest)?so=search",


        "Destination: Skellige (16)" to "https://witcher.fandom.com/wiki/Destination:_Skellige?so=search",


        "Flesh for Sale (12)" to "https://witcher.fandom.com/wiki/Flesh_for_Sale?so=search",


        "Broken Flowers (11)" to "https://witcher.fandom.com/wiki/Broken_Flowers?so=search",


        "Fencing Lessons (12)" to "https://witcher.fandom.com/wiki/Fencing_Lessons?so=search",


        "Following the Thread (11)" to "https://witcher.fandom.com/wiki/Following_the_Thread_(The_Witcher_3)?so=search",


        "Get Junior (12)" to "https://witcher.fandom.com/wiki/Get_Junior?so=search",


        "The Gangs of Novigrad (9)" to "https://witcher.fandom.com/wiki/The_Gangs_of_Novigrad?so=search",
        "Ciri's Story: Visiting Junior (9)" to "https://witcher.fandom.com/wiki/Ciri%27s_Story:_Visiting_Junior?so=search",
        "A Favor for Radovid (9)" to "https://witcher.fandom.com/wiki/A_Favor_for_Radovid?so=search",
        "Honor Among Thieves (9)" to "https://witcher.fandom.com/wiki/Honor_Among_Thieves?so=search",
        "A Warm Welcome" to "https://witcher.fandom.com/wiki/A_Warm_Welcome?so=search",
        "Strangers in the Night" to "https://witcher.fandom.com/wiki/Strangers_in_the_Night_(The_Witcher_3)?so=search",
        "A Barnful of Trouble (11)" to "https://witcher.fandom.com/wiki/A_Barnful_of_Trouble?so=search",
        "An Eye for an Eye (12)" to "https://witcher.fandom.com/wiki/An_Eye_for_an_Eye?so=search",
        "Count Reuven's Treasure (12)" to "https://witcher.fandom.com/wiki/Count_Reuven's_Treasure?so=search",

        "The Play's the Thing (11)" to "https://witcher.fandom.com/wiki/The_Play's_the_Thing?so=search",
        "Gwent: Playing Innkeeps (1)" to "https://witcher.fandom.com/wiki/Gwent:_Playing_Innkeeps?so=search",
        "A Poet Under Pressure (13)" to "https://witcher.fandom.com/wiki/A_Poet_Under_Pressure?so=search",


        "Ciri's Story: Breakneck Speed (11)" to "https://witcher.fandom.com/wiki/Ciri%27s_Story:_Breakneck_Speed?so=search",
        "In Ciri's Footsteps: Novigrad" to "https://witcher.fandom.com/wiki/In_Ciri%27s_Footsteps#:~:text=In%20Ciri's%20Footsteps%20is%20a,areas%20to%20complete%20this%20quest.",
        "A Dangerous Game (12)" to "https://witcher.fandom.com/wiki/A_Dangerous_Game?so=search",


        "A Tome Entombed (13)" to "https://witcher.fandom.com/wiki/A_Tome_Entombed?so=search",
        "Cabaret (14)" to "https://witcher.fandom.com/wiki/Cabaret?so=search",


        "Face Me if You Dare (III)" to "https://witcher.fandom.com/wiki/Face_Me_if_You_Dare!?so=search",
        "The Nobleman Statuette (14)" to "https://witcher.fandom.com/wiki/The_Nobleman_Statuette?so=search",
        "The Soldier Statuette (14)" to "https://witcher.fandom.com/wiki/The_Soldier_Statuette?so=search",
        "Races: The Great Erasmus Vegelbud Memorial Derby (1)" to "https://witcher.fandom.com/wiki/Race:_The_Great_Erasmus_Vegelbud_Memorial_Derby",
        "A Matter of Life and Death (12)" to "https://witcher.fandom.com/wiki/A_Matter_of_Life_and_Death_(The_Witcher_3)?so=search",


        "Witch Hunter Raids (II)" to "https://www.tivaprojects.com/witcher3map/v/index.html#5/215.328/105.031/m=215.328,105.031",
        "Novigrad, Closed City I (11)" to "https://www.tivaprojects.com/witcher3map/v/index.html#5/221.828/98.969/m=221.812,98.968",

        "Novigrad, Closed City II (1)" to "https://www.tivaprojects.com/witcher3map/v/index.html#5/210.562/95.609/m=210.562,95.609",
        "Carnal Sins (16)" to "https://witcher.fandom.com/wiki/Carnal_Sins?so=search",


        "Now or Never (14)" to "https://witcher.fandom.com/wiki/Now_or_Never?so=search",


        "A Deadly Plot (14)" to "https://witcher.fandom.com/wiki/A_Deadly_Plot?so=search",


        "Gwent: Playing Thaler (1)" to "https://witcher.fandom.com/wiki/Gwent:_Playing_Thaler?so=search",
        "The Price of Passage (I)" to "https://witcher.fandom.com/wiki/The_Price_of_Passage?so=search",

        "The Oxenfurt Drunk (26)" to "https://witcher.fandom.com/wiki/Contract:_The_Oxenfurt_Drunk?so=search",

        "The Price of Passage (II)" to "https://witcher.fandom.com/wiki/The_Price_of_Passage?so=search",
        "The Price of Passage (III)" to "https://witcher.fandom.com/wiki/The_Price_of_Passage?so=search",
        "Rough Neighborhood (10)" to "https://witcher.fandom.com/wiki/Rough_Neighborhood?so=search",

        "The Creature from the Oxenfurt Forest (35)" to "https://witcher.fandom.com/wiki/Contract:_The_Creature_from_Oxenfurt_Forest?so=search",


        "Message from an Old Friend (1)" to "https://witcher.fandom.com/wiki/Message_from_an_Old_Friend?so=search",
        "Empty Coop (1)" to "https://witcher.fandom.com/wiki/Empty_Coop?so=search",
        "The Dwarven Document Dilemma (2)" to "https://witcher.fandom.com/wiki/The_Dwarven_Document_Dilemma?so=search",
        "Hey, You Wanna Look at My Stuff? (6)" to "https://witcher.fandom.com/wiki/Hey,_You_Wanna_Look_at_my_Stuff%3F?so=search",
        "Novigrad Hospitality (8)" to "https://witcher.fandom.com/wiki/Novigrad_Hospitality?so=search",
        "Of Dairy and Darkness (9)" to "https://witcher.fandom.com/wiki/Of_Dairy_and_Darkness?so=search",


        "Spooked Mare (12)" to "https://witcher.fandom.com/wiki/Spooked_Mare?so=search",
        "Black Pearl (13)" to "https://witcher.fandom.com/wiki/Black_Pearl_(quest)?so=search",
        "Little Red (15)" to "https://witcher.fandom.com/wiki/Little_Red_(quest)?so=search",
        "A Feast for Crows (20)" to "https://witcher.fandom.com/wiki/A_Feast_for_Crows?so=search",
        "Of Swords and Dumplings (24)" to "https://witcher.fandom.com/wiki/Of_Swords_and_Dumplings?so=search",


        "A Final Kindness (26)" to "https://witcher.fandom.com/wiki/A_Final_Kindness?so=search",
        "An Elusive Thief (13)" to "https://witcher.fandom.com/wiki/Contract:_An_Elusive_Thief?so=search",
        "The Apiarian Phantom (14)" to "https://witcher.fandom.com/wiki/Contract:_The_Apiarian_Phantom?so=search",

        "The White Lady (16)" to "https://witcher.fandom.com/wiki/Contract:_The_White_Lady?so=search",
        "Doors Slamming Shut (24)" to "https://witcher.fandom.com/wiki/Contract:_Doors_Slamming_Shut?so=search",

        "Lord of the Wood (25)" to "https://witcher.fandom.com/wiki/Contract:_Lord_of_the_Wood?so=search",

        "Coast of Wrecks (13)" to "https://witcher.fandom.com/wiki/Coast_of_Wrecks_(quest)?so=search",
        "Battlefield Loot (20)" to "https://witcher.fandom.com/wiki/Battlefield_Loot?so=search",
        "Cat School Gear (Basic) (17)" to "https://witcher.fandom.com/wiki/Cat_School_Gear?so=search",
        "Cat School Part 1 (23)" to "https://witcher.fandom.com/wiki/Cat_School_Gear?so=search",
        "Gwent: Collect 'em All! (1)" to "https://witcher.fandom.com/wiki/Collect_'Em_All?so=search",
        "Gwent: Big City Players (1)" to "https://witcher.fandom.com/wiki/Gwent:_Big_City_Players?so=search",
        "Gwent: Old Pals (5)" to "https://witcher.fandom.com/wiki/Gwent:_Old_Pals?so=search",
        "Gwent: High Stakes (26)" to "https://witcher.fandom.com/wiki/High_Stakes?so=search",
        "Fists of Fury: Novigrad (23)" to "https://witcher.fandom.com/wiki/Fists_of_Fury:_Novigrad?so=search",


        "Hard Times (21)" to "https://witcher.fandom.com/wiki/Hard_Times?so=search",
        "The King is Dead - Long Live the King (16)" to "https://witcher.fandom.com/wiki/The_King_is_Dead_%E2%80%93_Long_Live_the_King?so=search",


        "Worthy of Trust (1)" to "https://witcher.fandom.com/wiki/Worthy_of_Trust?so=search",


        "Echoes of the Past (17)" to "https://witcher.fandom.com/wiki/Echoes_of_the_Past?so=search",
        "Taken as a Lass (25)" to "https://witcher.fandom.com/wiki/Taken_as_a_Lass?so=search",
        "Missing Persons (15)" to "https://witcher.fandom.com/wiki/Missing_Persons?so=search",

        "Nameless (14)" to "https://witcher.fandom.com/wiki/Nameless?so=search",
        "The Calm Before the Storm (14)" to "https://witcher.fandom.com/wiki/The_Calm_Before_the_Storm?so=search",
        "In Ciri's Footsteps: Skellige" to "https://witcher.fandom.com/wiki/In_Ciri%27s_Footsteps#:~:text=In%20Ciri's%20Footsteps%20is%20a,areas%20to%20complete%20this%20quest.",
        "In Ciri's Footsteps" to "https://witcher.fandom.com/wiki/In_Ciri%27s_Footsteps",
        "In Wolf's Clothing (15)" to "https://witcher.fandom.com/wiki/In_Wolf's_Clothing?so=search",
        "Redania's Most Wanted (12)" to "https://witcher.fandom.com/wiki/Redania's_Most_Wanted?so=search",


        "The Last Wish (15)" to "https://witcher.fandom.com/wiki/The_Last_Wish_(quest)?so=search",


        "Yustianna Disturbed" to "https://witcher.fandom.com/wiki/Yustianna_Disturbed?so=search",
        "Finders Keepers (24)" to "https://witcher.fandom.com/wiki/Finders_Keepers_(The_Witcher_3)?so=search",
        "Call of the Wild" to "https://witcher.fandom.com/wiki/Call_of_the_Wild?so=search",
        "Siren's Call" to "https://witcher.fandom.com/wiki/Siren's_Call?so=search",

        "Never Trust Children (II)" to "https://witcher.fandom.com/wiki/Never_Trust_Children?so=search",
        "The Phantom of Eldberg (17)" to "https://witcher.fandom.com/wiki/Contract:_The_Phantom_of_Eldberg?so=search",


        "Stranger in a Strange Land (14)" to "https://witcher.fandom.com/wiki/Stranger_in_a_Strange_Land?so=search",
        "The Cave of Dreams (14)" to "https://witcher.fandom.com/wiki/The_Cave_of_Dreams?so=search",


        "An Unpaid Debt (15)" to "https://witcher.fandom.com/wiki/An_Unpaid_Debt?so=search",
        "Farting Trolls" to "https://witcher.fandom.com/wiki/Farting_Trolls?so=search",
        "Possession (17)" to "https://witcher.fandom.com/wiki/Possession_(quest)?so=search",
        "The Lord of Undvik (17)" to "https://witcher.fandom.com/wiki/The_Lord_of_Undvik?so=search",


        "Deadly Delights (15)" to "https://witcher.fandom.com/wiki/Contract:_Deadly_Delights?so=search",


        "Out on Your Arse! (14)" to "https://witcher.fandom.com/wiki/Out_On_Your_Arse!?so=search",
        "Woe is Me" to "https://witcher.fandom.com/wiki/Woe_is_Me?so=search",
        "King's Gambit (18)" to "https://witcher.fandom.com/wiki/King's_Gambit?so=search",


        "Coronation (18)" to "https://witcher.fandom.com/wiki/Coronation?so=search",

        "The Four Faces of Hemdall" to "https://witcher.fandom.com/wiki/The_Four_Faces_of_Hemdall?so=search",

        "Brave Fools Die Young (1)" to "https://witcher.fandom.com/wiki/Brave_Fools_Die_Young?so=search",
        "Iron Maiden (19)" to "https://witcher.fandom.com/wiki/Iron_Maiden?so=search",
        "Fists of Fury: Skellige (30)" to "https://witcher.fandom.com/wiki/Fists_of_Fury:_Skellige?so=search",


        "A Hallowed Horn (12)" to "https://witcher.fandom.com/wiki/A_Hallowed_Horn?so=search",
        "From a Land Far, Far Away (13)" to "https://witcher.fandom.com/wiki/From_a_Land_Far,_Far_Away?so=search",

        "Free Spirit (13)" to "https://witcher.fandom.com/wiki/Free_Spirit?so=search",

        "Nithing (14)" to "https://witcher.fandom.com/wiki/The_Nithing?so=search",
        "Master of the Arena (14)" to "https://witcher.fandom.com/wiki/Master_of_the_Arena?so=search",
        "The Price of Honor (14)" to "https://witcher.fandom.com/wiki/The_Price_of_Honor?so=search",


        "The Family Blade (15)" to "https://witcher.fandom.com/wiki/The_Family_Blade?so=search",
        "For Fame and Glory (15)" to "https://witcher.fandom.com/wiki/For_Fame_and_Glory?so=search",
        "Armed Assault (15)" to "https://witcher.fandom.com/wiki/Armed_Assault?so=search",
        "A Bard's Beloved (15)" to "https://witcher.fandom.com/wiki/A_Bard's_Beloved?so=search",
        "The Path of Warriors (16)" to "https://witcher.fandom.com/wiki/The_Path_of_Warriors?so=search",
        "Crime and Punishment (18)" to "https://witcher.fandom.com/wiki/Crime_and_Punishment?so=search",

        "Shock Therapy (24)" to "https://witcher.fandom.com/wiki/Shock_Therapy?so=search",
        "Practicum in Advanced Alchemy (24)" to "https://witcher.fandom.com/wiki/Practicum_in_Advanced_Alchemy?so=search",
        "Abandoned Sawmill (24)" to "https://witcher.fandom.com/wiki/Abandoned_Sawmill_(quest)?so=search",
        "Peace Disturbed (25)" to "https://witcher.fandom.com/wiki/Peace_Disturbed?so=search",
        "The Sad Tale of the Grossbart Brothers (26)" to "https://witcher.fandom.com/wiki/The_Sad_Tale_of_the_Grossbart_Brothers?so=search",
        "The Tower Outta Nowheres (30)" to "https://witcher.fandom.com/wiki/The_Tower_Outta_Nowheres?so=search",
        "Strange Beast (16)" to "https://witcher.fandom.com/wiki/Contract:_Strange_Beast?so=search",
        "Muire D'yaeblen (18)" to "https://witcher.fandom.com/wiki/Contract:_Muire_D%27yaeblen?so=search",
        "Here Comes the Groom (19)" to "https://witcher.fandom.com/wiki/Contract:_Here_Comes_the_Groom?so=search",
        "In the Heart of the Woods (22)" to "https://witcher.fandom.com/wiki/In_the_Heart_of_the_Woods?so=search",
        "Missing Miners (27)" to "https://witcher.fandom.com/wiki/Contract:_Missing_Miners?so=search",
        "Dragon (28)" to "https://witcher.fandom.com/wiki/Contract:_Dragon?so=search",

        "Skellige's Most Wanted (29)" to "https://witcher.fandom.com/wiki/Contract:_Skellige%27s_Most_Wanted?so=search",

        "Missing Son (29)" to "https://witcher.fandom.com/wiki/Contract:_Missing_Son?so=search",
        "Freya Be Praised! (4)" to "https://witcher.fandom.com/wiki/Freya_Be_Praised!?so=search",
        "Not Only Eagles Dare (10)" to "https://witcher.fandom.com/wiki/Not_Only_Eagles_Dare?so=search",
        "X Marks the Spot (12)" to "https://witcher.fandom.com/wiki/X_Marks_the_Spot?so=search",
        "Family Fortune (13)" to "https://witcher.fandom.com/wiki/Family_Fortune?so=search",
        "Pearls of the Coast (13)" to "https://witcher.fandom.com/wiki/Pearls_of_the_Coast?so=search",
        "Nilfgaardian Treasure (13)" to "https://witcher.fandom.com/wiki/Nilfgaardian_Treasure?so=search",
        "Precious Haul (13)" to "https://witcher.fandom.com/wiki/Precious_Haul?so=search",
        "Shortcut (13)" to "https://witcher.fandom.com/wiki/Shortcut?so=search",
        "Ironsides' Treasure (13)" to "https://witcher.fandom.com/wiki/Ironsides'_Treasure?so=search",
        "Inheritance (14)" to "https://witcher.fandom.com/wiki/Inheritance?so=search",
        "Ruins, Hidden Treasure, You Know... (18)" to "https://witcher.fandom.com/wiki/Ruins,_Hidden_Treasure,_You_Know...?so=search",
        "Hidden in the Depths (31)" to "https://witcher.fandom.com/wiki/Hidden_in_the_Depths?so=search",
        "Unlucky's Treasure (48)" to "https://witcher.fandom.com/wiki/Unlucky's_Treasure?so=search",
        "Ursine School Gear (Basic) (20)" to "https://witcher.fandom.com/wiki/Bear_School_Gear?so=search",
        "Ursine School Part 1 (25)" to "https://witcher.fandom.com/wiki/Bear_School_Gear?so=search",
        "Ursine School Part 2 (25)" to "https://witcher.fandom.com/wiki/Bear_School_Gear?so=search",
        "Ursine School Part 3 (30)" to "https://witcher.fandom.com/wiki/Bear_School_Gear?so=search",
        "Ursine School Part 4 (34)" to "https://witcher.fandom.com/wiki/Bear_School_Gear?so=search",
        "Cat School Part 4 (34)" to "https://witcher.fandom.com/wiki/Cat_School_Gear?so=search",
        "Griffin School Part 3 (26)" to "https://witcher.fandom.com/wiki/Griffin_School_Gear?so=search",
        "Griffin School Part 4 (34)" to "https://witcher.fandom.com/wiki/Griffin_School_Gear?so=search",
        "Wolf School Part 3 (29)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Wolf School Part 6 (34)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Gwent: Collect 'em All! (1)" to "https://witcher.fandom.com/wiki/Collect_'Em_All?so=search",
        "Gwent: Skellige Style (1)" to "https://witcher.fandom.com/wiki/Gwent:_Skellige_Style?so=search",
        "Fists of Fury: Champion of Champions (11)" to "https://witcher.fandom.com/wiki/Fists_of_Fury:_Champion_of_Champions?so=search",
        "The Heroes' Pursuits: Fayrlund (1)" to "https://witcher.fandom.com/wiki/The_Heroes%27_Pursuits:_Fayrlund?so=search",
        "The Heroes' Pursuits: Fyresdal (1)" to "https://witcher.fandom.com/wiki/The_Heroes%27_Pursuits:_Fyresdal?so=search",
        "The Heroes' Pursuits: Kaer Trolde (1)" to "https://witcher.fandom.com/wiki/The_Heroes%27_Pursuits:_Kaer_Trolde?so=search",
        "The Heroes' Pursuits: For the Goddess' Glory! (1)" to "https://witcher.fandom.com/wiki/The_Heroes%27_Pursuits:_For_the_Goddess%27_Glory!?so=search",


        "Ugly Baby (19)" to "https://witcher.fandom.com/wiki/Ugly_Baby?so=search",

        "Disturbance (1)" to "https://witcher.fandom.com/wiki/Disturbance?so=search",
        "To Bait a Forktail... (19)" to "https://witcher.fandom.com/wiki/To_Bait_a_Forktail...?so=search",
        "The Final Trial (19)" to "https://witcher.fandom.com/wiki/The_Final_Trial?so=search",


        "Gwent: Old Pals (5)" to "https://witcher.fandom.com/wiki/Gwent:_Old_Pals?so=search",
        "Berengar's Blade (27)" to "https://witcher.fandom.com/wiki/Berengar's_Blade?so=search",

        "Trail of Echoes" to "https://witcher.fandom.com/wiki/Trail_of_Echoes?so=search",
        "No Place Like Home (19)" to "https://witcher.fandom.com/wiki/No_Place_Like_Home?so=search",


        "Va Fail Elaine (19)" to "https://witcher.fandom.com/wiki/Va_Fail,_Elaine?so=search",


        "Bastion (23)" to "https://witcher.fandom.com/wiki/Bastion?so=search",

        "Monster Slayer (26)" to "https://witcher.fandom.com/wiki/Monster_Slayer?so=search",
        "Greenhouse Effect (27)" to "https://witcher.fandom.com/wiki/Greenhouse_Effect?so=search",
        "The Witchers' Forge (30)" to "https://witcher.fandom.com/wiki/The_Witchers'_Forge?so=search",
        "Wolf School Gear (Basic) (14)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Wolf School Part 2 (21)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Wolf School Part 4 (29)" to "https://witcher.fandom.com/wiki/Wolf_School_Gear?so=search",
        "Forgotten Wolf School Gear Part 1 (34)" to "https://witcher.fandom.com/wiki/Scavenger_Hunt:_Forgotten_Wolf_School_Gear_Diagrams?so=search",
        "Forgotten Wolf School Gear Part 2 (40)" to "https://witcher.fandom.com/wiki/Forgotten_Wolf_School_Gear?so=search",
        "Gwent: Collect 'em All! (1)" to "https://witcher.fandom.com/wiki/Collect_'Em_All?so=search",


        "Brothers in Arms: Velen (22)" to "https://witcher.fandom.com/wiki/Brothers_In_Arms:_Velen?so=search",
        "Brothers in Arms: Novigrad (22)" to "https://witcher.fandom.com/wiki/Brothers_In_Arms:_Novigrad?so=search",
        "Brothers in Arms: Nilfgaard (22)" to "https://witcher.fandom.com/wiki/Brothers_In_Arms:_Nilfgaard?so=search",
        "Brothers in Arms: Skellige (22)" to "https://witcher.fandom.com/wiki/Brothers_In_Arms:_Skellige?so=search",
        "The Isle of Mists (22)" to "https://witcher.fandom.com/wiki/The_Isle_of_Mists_(quest)?so=search",


        "The Battle of Kaer Morhen (24)" to "https://witcher.fandom.com/wiki/The_Battle_of_Kaer_Morhen?so=search",


        "Blood on the Battlefield (20)" to "https://witcher.fandom.com/wiki/Blood_on_the_Battlefield?so=search",


        "Bald Mountain (26)" to "https://witcher.fandom.com/wiki/Bald_Mountain_(quest)?so=search",


        "Final Preparations (1)" to "https://witcher.fandom.com/wiki/Final_Preparations?so=search",
        "It Takes Three to Tango (28)" to "https://witcher.fandom.com/wiki/It_Takes_Three_to_Tango?so=search",
        "Blindingly Obvious (28)" to "https://witcher.fandom.com/wiki/Blindingly_Obvious?so=search",


        "Reason of State (30)" to "https://witcher.fandom.com/wiki/Reason_of_State?so=search",

        "The Great Escape (28)" to "https://witcher.fandom.com/wiki/The_Great_Escape?so=search",
        "Payback (28)" to "https://witcher.fandom.com/wiki/Payback?so=search",
        "Through Time and Space (26)" to "https://witcher.fandom.com/wiki/Through_Time_and_Space?so=search",
        "Battle Preparations (28)" to "https://witcher.fandom.com/wiki/Battle_Preparations?so=search",
        "The Sunstone (28)" to "https://witcher.fandom.com/wiki/The_Sunstone?so=search",

        "Veni Vidi Vigo (28)" to "https://witcher.fandom.com/wiki/Veni_Vidi_Vigo?so=search",
        "Child of the Elder Blood (31)" to "https://witcher.fandom.com/wiki/Child_of_the_Elder_Blood?so=search",

        "Skjall's Grave (28)" to "https://witcher.fandom.com/wiki/Skjall's_Grave?so=search",
        "On Thin Ice (30)" to "https://witcher.fandom.com/wiki/On_Thin_Ice?so=search",


        "Tedd Deireadh, The Final Age (30)" to "https://witcher.fandom.com/wiki/Tedd_Deireadh,_The_Final_Age?so=search",

        "Faithful Friend (30)" to "https://witcher.fandom.com/wiki/Faithful_Friend?so=search",
        "Something Ends, Something Begins (30)" to "https://witcher.fandom.com/wiki/Something_Ends,_Something_Begins_(quest)?so=search",


        "Evil's First Soft Touches (32)" to "https://witcher.fandom.com/wiki/Evil's_Soft_First_Touches?so=search",

        "Dead Man's Party (33)" to "https://witcher.fandom.com/wiki/Dead_Man's_Party?so=search",


        "The Cursed Chapel (34)" to "https://witcher.fandom.com/wiki/The_Cursed_Chapel?so=search",

        "A Midnight Clear (33)" to "https://witcher.fandom.com/wiki/A_Midnight_Clear?so=search",
        "Viper Armour Set (34)" to "https://witcher.fandom.com/wiki/Viper_School_Gear?so=search",

        "Venomous Viper Steel Sword (34)" to "https://witcher.fandom.com/wiki/Viper_School_Gear?so=search",
        "Open Sesame! Part 1 (34)" to "https://witcher.fandom.com/wiki/Open_Sesame!_(Hearts_of_Stone)?so=search",


        "Open Sesame: The Safecracker (34)" to "https://witcher.fandom.com/wiki/Open_Sesame:_The_Safecracker",


        "Open Sesame: Breaking and Entering (34)" to "https://witcher.fandom.com/wiki/Open_Sesame:_Breaking_and_Entering",
        "Open Sesame: Witcher Seasonings" to "https://witcher.fandom.com/wiki/Open_Sesame:_Witcher_Seasonings",
        "Open Sesame! Part 2: The Heist" to "https://witcher.fandom.com/wiki/Open_Sesame!_(Hearts_of_Stone)?so=search",


        "A Dark Legacy (34)" to "https://witcher.fandom.com/wiki/A_Dark_Legacy?so=search",
        "Avid Collector (34)" to "https://witcher.fandom.com/wiki/Avid_Collector?so=search",


        "Scenes From a Marriage (35)" to "https://witcher.fandom.com/wiki/Scenes_From_a_Marriage?so=search",


        "Venomous Viper Silver Sword  (36)" to "https://witcher.fandom.com/wiki/Viper_School_Gear?so=search",
        "Whatsoever a Man Soweth (36)" to "https://witcher.fandom.com/wiki/Whatsoever_a_Man_Soweth...?so=search",


        "Enchanting: Start-up Costs (32)" to "https://witcher.fandom.com/wiki/Enchanting:_Start-up_Costs?so=search",
        "Enchanting: Quality Has Its Price (32)" to "https://witcher.fandom.com/wiki/Enchanting:_Quality_Has_Its_Price?so=search",

        "The Taxman Cometh (32)" to "https://witcher.fandom.com/wiki/The_Taxman_Cometh?so=search",
        "Without a Trace (32)" to "https://witcher.fandom.com/wiki/Without_a_Trace?so=search",


        "Roses on a Red Field (33)" to "https://witcher.fandom.com/wiki/Rose_on_a_Red_Field?so=search",
        "Tinker, Hunter, Soldier, Spy (33)" to "https://witcher.fandom.com/wiki/Tinker,_Hunter,_Soldier,_Spy?so=search",
        "From Ofier's Distant Shores (33)" to "https://witcher.fandom.com/wiki/From_Ofier's_Distant_Shores?so=search",
        "The Royal Air Force (36)" to "https://witcher.fandom.com/wiki/The_Royal_Air_Force?so=search",

        "The Sword, Famine and Perfidy (36)" to "https://witcher.fandom.com/wiki/The_Sword,_Famine_and_Perfidy?so=search",
        "The Secret Life of Count Romilly (38)" to "https://witcher.fandom.com/wiki/The_Secret_Life_of_Count_Romilly?so=search",
        "The Drakenborg Redemption (38)" to "https://witcher.fandom.com/wiki/The_Drakenborg_Redemption?so=search",
        "A Surprise Inheritance (38)" to "https://witcher.fandom.com/wiki/A_Surprise_Inheritance?so=search",
        "Races: Swift as the Western Winds (32)" to "https://witcher.fandom.com/wiki/Races:_Swift_as_the_Western_Winds?so=search",


        "Envoys, Wineboys (34)" to "https://witcher.fandom.com/wiki/Envoys,_Wineboys",


        "The Beast of Toussaint (35)" to "https://witcher.fandom.com/wiki/The_Beast_of_Toussaint?so=search",


        "Blood Run (36)" to "https://witcher.fandom.com/wiki/Blood_Run?so=search",

        "No Place Like Home (34)" to "https://witcher.fandom.com/wiki/No_Place_Like_Home_(Blood_and_Wine)?so=search",

        "Turn and Face the Strange (35)" to "https://witcher.fandom.com/wiki/Turn_and_Face_the_Strange?so=search",


        "There Can Be Only One (43)" to "https://witcher.fandom.com/wiki/There_Can_Be_Only_One?so=search",
        "Fists of Fury: Toussaint (40)" to "https://witcher.fandom.com/wiki/Fists_of_Fury:_Toussaint?so=search",

        "Raging Wolf (40)" to "https://witcher.fandom.com/wiki/Raging_Wolf?so=search",

        "Wine Wars: Belgaard Part 1 (39)" to "https://witcher.fandom.com/wiki/Wine_Wars:_Belgaard?so=search",

        "Wine Wars: Coronata (37)" to "https://witcher.fandom.com/wiki/Wine_Wars:_Coronata?so=search",
        "Wine Wars: Vermentino (37)" to "https://witcher.fandom.com/wiki/Wine_Wars:_Vermentino?so=search",
        "Wine Wars: The Deus in the Machina (42)" to "https://witcher.fandom.com/wiki/Wine_Wars:_The_Deus_in_the_Machina?so=search",
        "Wine Wars: Consorting (40)" to "https://witcher.fandom.com/wiki/Wine_Wars:_Consorting?so=search",
        "Wine Wars: Belgaard Part 2 (39)" to "https://witcher.fandom.com/wiki/Wine_Wars:_Belgaard?so=search",
        "The Warble of a Smitten Knight (35)" to "https://witcher.fandom.com/wiki/The_Warble_of_a_Smitten_Knight?so=search",


        "The Last Exploits of Selina's Gang (37)" to "https://witcher.fandom.com/wiki/The_Last_Exploits_of_Selina's_Gang?so=search",

        "Till Death Do You Part (36)" to "https://witcher.fandom.com/wiki/Till_Death_Do_You_Part?so=search",


        "Bovine Blues (38)" to "https://witcher.fandom.com/wiki/Contract:_Bovine_Blues?so=search",

        "Extreme Cosplay (40)" to "https://witcher.fandom.com/wiki/Extreme_Cosplay?so=search",


        "La Cage au Fou (39)" to "https://witcher.fandom.com/wiki/La_Cage_au_Fou?so=search",


        "Spoontaneous Profits! (42)" to "https://witcher.fandom.com/wiki/Spoontaneous_Profits!?so=search",


        "Amidst the Mill's Grist (42)" to "https://witcher.fandom.com/wiki/Amidst_the_Mill's_Grist?so=search",
        "The Hunger Game (42)" to "https://witcher.fandom.com/wiki/The_Hunger_Game?so=search",

        "Where Children Toil, Toys Waste Away (42)" to "https://witcher.fandom.com/wiki/Where_Children_Toil,_Toys_Waste_Away?so=search",


        "Wine is Sacred (42)" to "https://witcher.fandom.com/wiki/Wine_is_Sacred?so=search",

        "A Portrait of the Witcher as an Old Man (43)" to "https://witcher.fandom.com/wiki/A_Portrait_of_the_Witcher_as_an_Old_Man?so=search",


        "Of Sheers and a Witcher I Sing (43)" to "https://witcher.fandom.com/wiki/Of_Sheers_and_a_Witcher_I_Sing?so=search",

        "The Man from Cintra (43)" to "https://witcher.fandom.com/wiki/The_Man_from_Cintra?so=search",


        "Capture the Castle (47)" to "https://witcher.fandom.com/wiki/Capture_the_Castle?so=search",


        "The Night of Long Fangs (47)" to "https://witcher.fandom.com/wiki/The_Night_of_Long_Fangs?so=search",


        "Duck, Duck, Goosed! (47)" to "https://witcher.fandom.com/wiki/Duck,_Duck,_Goosed!?so=search",

        "Beyond Hill and Dale... (47)" to "https://witcher.fandom.com/wiki/Beyond_Hill_and_Dale...?so=search",


        "Tesham Mutna (49)" to "https://witcher.fandom.com/wiki/Tesham_Mutna?so=search",
        "Pomp and Strange Circumstance (49)" to "https://witcher.fandom.com/wiki/Pomp_and_Strange_Circumstance?so=search",


        "B) Burlap is the New Stripe (49)" to "https://witcher.fandom.com/wiki/Burlap_is_the_New_Stripe?so=search",


        "The Perks of Being a Jailbird (49)" to "https://witcher.fandom.com/wiki/The_Perks_of_Being_a_Jailbird?so=search",
        "Using Your Loaf (-)" to "https://witcher.fandom.com/wiki/Burlap_is_the_New_Stripe",
        "Be It Ever So Humble... (49)" to "https://witcher.fandom.com/wiki/Be_It_Ever_So_Humble...?so=search",


        "Blood Simple (47)" to "https://witcher.fandom.com/wiki/Blood_Simple?so=search",

        "What Lies Unseen (47)" to "https://witcher.fandom.com/wiki/What_Lies_Unseen?so=search",


        "Tesham Mutna (49)" to "https://witcher.fandom.com/wiki/Tesham_Mutna?so=search",
        "Pomp and Strange Circumstance (49)" to "https://witcher.fandom.com/wiki/Pomp_and_Strange_Circumstance?so=search",


        "Be It Ever So Humble... (49)" to "https://witcher.fandom.com/wiki/Be_It_Ever_So_Humble...?so=search",
        "Paperchase (36)" to "https://witcher.fandom.com/wiki/Paperchase?so=search",

        "Goodness, Gracious, Great Balls of Granite! (36)" to "https://witcher.fandom.com/wiki/Goodness,_Gracious,_Great_Balls_of_Granite!?so=search",
        "Father Knows Worst (37)" to "https://witcher.fandom.com/wiki/Father_Knows_Worst?so=search",

        "A Knight's Tales (40)" to "https://witcher.fandom.com/wiki/A_Knight's_Tales?so=search",
        "Big Feet to Fill (40)" to "https://witcher.fandom.com/wiki/Big_Feet_to_Fill?so=search",
        "Big Feet to Fill: The First Group (40)" to "https://witcher.fandom.com/wiki/Big_Feet_to_Fill:_The_First_Group?so=search",
        "Big Feet to Fill: The Second Group (40)" to "https://witcher.fandom.com/wiki/Big_Feet_to_Fill:_The_Second_Group?so=search",
        "Big Feet to Fill: The Third Group (40)" to "https://witcher.fandom.com/wiki/Big_Feet_to_Fill:_The_Third_Group?so=search",
        "Big Feet to Fill: The Fourth Group (40)" to "https://witcher.fandom.com/wiki/Big_Feet_to_Fill:_The_Fourth_Group?so=search",
        "Big Feet to Fill: The Fifth Group (40)" to "https://witcher.fandom.com/wiki/Big_Feet_to_Fill:_The_Fifth_Group?so=search",
        "Master Master Master Master! (40)" to "https://witcher.fandom.com/wiki/Master_Master_Master_Master!?so=search",
        "The Words of the Prophets Are Written on Sarcophagi (40)" to "https://witcher.fandom.com/wiki/The_Words_of_the_Prophets_Are_Written_on_Sarcophagi?so=search",
        "Mutual of Beauclair's Wild Kingdom (46)" to "https://witcher.fandom.com/wiki/Mutual_of_Beauclair's_Wild_Kingdom?so=search",

        "Knight for Hire (-)" to "https://witcher.fandom.com/wiki/Knight_for_Hire?so=search",
        "Big Game Hunter (37)" to "https://witcher.fandom.com/wiki/Big_Game_Hunter?so=search",


        "Vitner's Contract: Rivecalme Storehouse (37)" to "https://witcher.fandom.com/wiki/Vintner%27s_Contract:_Rivecalme_Storehouse",
        "Vitner's Contract: Chuchote Cave (40)" to "https://witcher.fandom.com/wiki/Vintner%27s_Contract:_Chuchote_Cave",
        "Vitner's Contract: Cleaning Those Hard-to-Reach Places (40)" to "https://witcher.fandom.com/wiki/Vintner%27s_Contract:_Cleaning_Those_Hard-to-Reach_Places",
        "Vitner's Contract: Dun Tynne Hillside (40)" to "https://witcher.fandom.com/wiki/Vintner%27s_Contract:_Dun_Tynne_Hillside#:~:text=Vintner's%20Contract%3A%20Dun%20Tynne%20Hillside%20is%20a%20secondary%20quest,the%20Blood%20and%20Wine%20expansion.&text=rewarded%20in%20this%20quest%20will,reaching%20level%2045%2C%20not%2046.",
        "Vitner's Contract: Duchaton Crest (43)" to "https://witcher.fandom.com/wiki/Vintner%27s_Contract:_Duchaton_Crest",
        "Equine Phantoms (44)" to "https://witcher.fandom.com/wiki/Equine_Phantoms?so=search",


        "Feet as Cold as Ice (45)" to "https://witcher.fandom.com/wiki/Feet_as_Cold_as_Ice?so=search",
        "The Tufo Monster (48)" to "https://witcher.fandom.com/wiki/Contract:_The_Tufo_Monster?so=search",
        "Coin Doesn't Stink (37)" to "https://witcher.fandom.com/wiki/Coin_Doesn't_Stink?so=search",
        "Don't Take Candy from a Stranger (37)" to "https://witcher.fandom.com/wiki/Don't_Take_Candy_from_a_Stranger?so=search",
        "The Black Widow (37)" to "https://witcher.fandom.com/wiki/The_Black_Widow?so=search",
        "The Inconstant Gardener (37)" to "https://witcher.fandom.com/wiki/The_Inconstant_Gardener?so=search",
        "Applied Escapology (40)" to "https://witcher.fandom.com/wiki/Applied_Escapology?so=search",
        "Around the World in...Eight Days (40)" to "https://witcher.fandom.com/wiki/Around_the_World_in..._Eight_Days?so=search",
        "Waiting for Goe and Doh (40)" to "https://witcher.fandom.com/wiki/Waiting_for_Goe_and_Doh?so=search",
        "But Other Than That, How Did You Enjoy the Play? (43)" to "https://witcher.fandom.com/wiki/But_Other_Than_That,_How_Did_You_Enjoy_the_Play%3F?so=search",
        "What Was This About Again? (43)" to "https://witcher.fandom.com/wiki/What_Was_This_About_Again%3F?so=search",
        "The Curse of Carnarvon (46)" to "https://witcher.fandom.com/wiki/The_Curse_of_Carnarvon?so=search",
        "The Suffering of Young Francois (47)" to "https://witcher.fandom.com/wiki/The_Suffering_of_Young_Francois?so=search",

        "The Toussaint Prison Experient (47)" to "https://witcher.fandom.com/wiki/The_Toussaint_Prison_Experiment?so=search",
        "Filibert Always Pays His Debts (48)" to "https://witcher.fandom.com/wiki/Filibert_Always_Pays_His_Debts?so=search",
        "Grandmaster Feline Gear (40)" to "https://witcher.fandom.com/wiki/Scavenger_Hunt:_Grandmaster_Feline_Gear",

        "Grandmaster Griffin Gear (40)" to "https://witcher.fandom.com/wiki/Scavenger_Hunt:_Grandmaster_Griffin_Gear",

        "Grandmaster Manticore Gear (40)" to "https://witcher.fandom.com/wiki/Scavenger_Hunt:_Grandmaster_Manticore_Gear",

        "Grandmaster Ursine Gear (40)" to "https://witcher.fandom.com/wiki/Scavenger_Hunt:_Grandmaster_Ursine_Gear",
        "Grandmaster Wolven Gear (40)" to "https://witcher.fandom.com/wiki/Scavenger_Hunt:_Grandmaster_Wolven_Gear",
        "Gwent: Never Fear, Skellige's Here (1)" to "https://witcher.fandom.com/wiki/Gwent:_Never_Fear,_Skellige%27s_Here?so=search",

        "Gwent: To Everything - Turn, Turn, Tournament! (38)" to "https://witcher.fandom.com/wiki/Gwent:_To_Everything_-_Turn,_Turn,_Tournament!?so=search",
    )

    private val anyOrderStart = "THE FOLLOWING QUESTS CAN BE DONE AT ANY TIME IN ANY ORDER"

    private val df = DataFrame.readCSV(inputStream)

    override fun all(): List<Quest> {
        var orderIdx = 1
        var withoutOrder = false
        var storyBranch: String? = null

        return df
            .fillNulls("Quest").with {
                val row = this
                fun prevVal(addDataRow: DataRow<Any?>?): Any? {
                    var currentRow = addDataRow
                    while (currentRow != null) {
                        val name = currentRow.get("Quest")
                        if (name != null || currentRow.index() == 0) {
                            return name
                        }
                        currentRow = currentRow.prev()
                    }
                    return null
                }

                if (row["Detail Completed"] != null) prevVal(this)
                else null
            }
            .filter {
                val location = it["Location"] as String?
                it["Quest"] != null ||
                        location == anyOrderStart ||
                        location == "ORDER-END-MARKER" ||
                        location?.contains("STORY BRANCH") == true
            }
            .fillNullsWithPrev("Location")
            .map { row ->
                val location = row["Location"] as String
                val quest = row["Quest"] as String?

                if (location == anyOrderStart) {
                    withoutOrder = true
                    storyBranch = null
                } else if (location.contains("STORY BRANCH")) {
                    storyBranch = location
                } else if (location == "ORDER-END-MARKER") {
                    storyBranch = null
                    withoutOrder = false
                }

                if (quest == null) {
                    return@map null
                }

                val (name, level) = extractLevel(quest)

                val detail = row["Details"] as String?

                QuestFlat(
                    location = location,
                    quest = name,
                    url = questLinks[quest] ?: throw IllegalArgumentException("No link for $quest"),
                    isCompleted = false,
                    suggested = level,
                    branch = storyBranch,
                    order = if (withoutOrder) Order.Any else Order.Suggested(orderIdx++),
                    detail = detail?.let { ExtraDetail(it, null, false) },
                )
            }
            .filterNotNull()
            .fold(mutableMapOf<QuestKey, Pair<MutableList<ExtraDetail>, Order>>()) { acc, q ->
                val key = QuestKey(
                    location = q.location,
                    quest = q.quest,
                    isCompleted = q.isCompleted,
                    suggested = q.suggested,
                    url = q.url,
                    branch = q.branch,
                )
                val details = acc.getOrPut(key) { Pair(mutableListOf(), q.order) }
                q.detail?.let { details.first.add(it) }
                acc
            }
            .map { (key, pair) ->
                Quest(
                    location = key.location,
                    quest = key.quest,
                    isCompleted = key.isCompleted,
                    suggested = key.suggested,
                    url = key.url,
                    branch = key.branch,
                    order = pair.second,
                    extraDetails = pair.first,
                )
            }
    }

    private fun extractLevel(input: String): Pair<String, Level> {
        val regex = "^(.*?)\\s*(?:\\((\\d+)\\))?$".toRegex()
        val matchResult = regex.find(input)
        val firstPart = matchResult?.groups?.get(1)?.value ?: ""
        val level = matchResult?.groups?.get(2)?.value?.toInt()
        val levelObj = if (level == null) Level.Unaffected else Level.Suggested(level)
        return Pair(firstPart, levelObj)
    }

    private fun <T> DataFrame<T>.fillNullsWithPrev(column: String): DataFrame<T> =
        fillNulls(column).with {
            fun prevVal(addDataRow: DataRow<Any?>?): Any? {
                var currentRow = addDataRow
                while (currentRow != null) {
                    val name = currentRow.get(column)
                    if (name != null || currentRow.index() == 0) {
                        return name
                    }
                    currentRow = currentRow.prev()
                }
                return null
            }
            prevVal(this)
        }
}