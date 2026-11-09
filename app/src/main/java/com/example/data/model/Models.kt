package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class Rarity(val displayName: String, val badgeColor: Long) {
    COMMON("Common", 0xFF81C784),
    UNCOMMON("Uncommon", 0xFF4CAF50),
    RARE("Rare", 0xFF29B6F6),
    EPIC("Epic", 0xFFAB47BC),
    LEGENDARY("Legendary", 0xFFFFA726),
    SEASONAL("Seasonal", 0xFFFF7043)
}

data class TreeSpecies(
    val id: String,
    val name: String,
    val description: String,
    val rarity: Rarity,
    val price: Int,
    val unlockLevel: Int,
    val foliageColor: Long,
    val foliageAccentColor: Long,
    val trunkColor: Long,
    val isPremium: Boolean = false,
    val shapeType: TreeShapeType = TreeShapeType.PINE
)

enum class TreeShapeType {
    PINE,
    ROUND_OAK,
    SAKURA,
    WEEPING_WILLOW,
    PALM,
    BONSAI,
    GINKGO,
    MAPLE,
    CELESTIAL,
    BAOBAB,
    JACARANDA,
    REDWOOD,
    WISTERIA,
    CRYSTAL_TREE,
    BAMBOO
}

enum class ForestEnvironmentType(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: Int,
    val groundColor: Long,
    val skyTopColor: Long,
    val skyBottomColor: Long,
    val particleColor: Long
) {
    VERDANT_MEADOW(
        "meadow", "Verdant Meadow", "Lush green grass under clear blue skies",
        0, 0xFF4E7A58, 0xFF81D4FA, 0xFFE1F5FE, 0x88FFFFFF
    ),
    CHERRY_VALLEY(
        "cherry_valley", "Cherry Valley", "Gentle pink petals drifting through spring air",
        300, 0xFF6B8E6F, 0xFFFFCDD2, 0xFFFCE4EC, 0x88F48FB1
    ),
    AMBER_RIDGE(
        "amber_ridge", "Amber Ridge", "Crisp autumn breeze with golden and copper hues",
        400, 0xFF8D5B4C, 0xFFFFE082, 0xFFFFF8E1, 0x88FFB74D
    ),
    FROST_PEAK(
        "frost_peak", "Frost Peak", "Quiet winter solitude with gentle snowfall",
        500, 0xFF78909C, 0xFFCFD8DC, 0xFFECEFF1, 0x88FFFFFF
    ),
    KYOTO_GARDEN(
        "kyoto_garden", "Kyoto Garden", "Zen peaceful stones, bamboo, and tranquil ponds",
        650, 0xFF455A64, 0xFFB0BEC5, 0xFFECEFF1, 0x88E0F2F1
    ),
    FIREFLY_SANCTUARY(
        "firefly_sanctuary", "Firefly Sanctuary", "Magical twilight with glowing bioluminescent fireflies",
        700, 0xFF1B2A22, 0xFF0D1B2A, 0xFF1B263B, 0xAAFFF59D
    )
}

enum class FocusMode(val title: String) {
    COUNTDOWN("Countdown"),
    STOPWATCH("Stopwatch"),
    POMODORO("Pomodoro")
}

enum class SessionStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    INTERRUPTED
}

enum class TreeGrowthStage(val progressThreshold: Float) {
    SEED(0.0f),
    SPROUT(0.2f),
    SAPLING(0.45f),
    YOUNG_TREE(0.75f),
    MATURE_TREE(1.0f);

    companion object {
        fun fromProgress(progress: Float): TreeGrowthStage {
            return when {
                progress >= 0.95f -> MATURE_TREE
                progress >= 0.65f -> YOUNG_TREE
                progress >= 0.35f -> SAPLING
                progress >= 0.10f -> SPROUT
                else -> SEED
            }
        }
    }
}

enum class FocusCategory(val title: String, val colorHex: Long) {
    STUDYING("Studying", 0xFF2E7D32),
    WORK("Work", 0xFF1565C0),
    READING("Reading", 0xFF7B1FA2),
    PROGRAMMING("Coding", 0xFF00838F),
    WRITING("Writing", 0xFFC2185B),
    EXERCISE("Exercise", 0xFFD84315),
    MINDFULNESS("Mindfulness", 0xFF00695C),
    CREATIVE("Creative", 0xFFE65100),
    OTHER("Other", 0xFF546E7A)
}

enum class AmbientSoundType(val title: String, val iconName: String) {
    NONE("Silent", "volume_off"),
    RAIN("Gentle Rain", "water_drop"),
    DEEP_FOREST("Deep Forest", "park"),
    OCEAN_WAVES("Ocean Waves", "tsunami"),
    CAMPFIRE("Campfire", "local_fire_department"),
    NIGHT_CRICKETS("Night Crickets", "nightlight"),
    WHITE_NOISE("White Noise", "grain")
}

val ALL_TREE_SPECIES = listOf(
    TreeSpecies(
        id = "classic_pine",
        name = "Classic Pine",
        description = "Resilient evergreen needle tree with deep forest green cones. Grows reliably in any climate.",
        rarity = Rarity.COMMON,
        price = 0,
        unlockLevel = 1,
        foliageColor = 0xFF2E7D32,
        foliageAccentColor = 0xFF43A047,
        trunkColor = 0xFF5D4037,
        shapeType = TreeShapeType.PINE
    ),
    TreeSpecies(
        id = "ancient_oak",
        name = "Ancient Oak",
        description = "Mighty, sprawling oak representing deep concentration and wisdom.",
        rarity = Rarity.COMMON,
        price = 0,
        unlockLevel = 1,
        foliageColor = 0xFF388E3C,
        foliageAccentColor = 0xFF66BB6A,
        trunkColor = 0xFF4E342E,
        shapeType = TreeShapeType.ROUND_OAK
    ),
    TreeSpecies(
        id = "silver_birch",
        name = "Silver Birch",
        description = "Slender chalk-white trunk with delicate fluttering golden-green leaves.",
        rarity = Rarity.COMMON,
        price = 150,
        unlockLevel = 1,
        foliageColor = 0xFF9CCC65,
        foliageAccentColor = 0xFFC5E1A5,
        trunkColor = 0xFFECEFF1,
        shapeType = TreeShapeType.ROUND_OAK
    ),
    TreeSpecies(
        id = "zen_bamboo",
        name = "Zen Bamboo",
        description = "Harmonious cluster of graceful emerald stalks and whispering jade leaves that bend without breaking.",
        rarity = Rarity.UNCOMMON,
        price = 220,
        unlockLevel = 2,
        foliageColor = 0xFF43A047,
        foliageAccentColor = 0xFF7CB342,
        trunkColor = 0xFF2E7D32,
        shapeType = TreeShapeType.BAMBOO
    ),
    TreeSpecies(
        id = "autumn_maple",
        name = "Crimson Maple",
        description = "Vibrant flaming scarlet and amber foliage with star-shaped leaves.",
        rarity = Rarity.UNCOMMON,
        price = 250,
        unlockLevel = 2,
        foliageColor = 0xFFD84315,
        foliageAccentColor = 0xFFFF7043,
        trunkColor = 0xFF3E2723,
        shapeType = TreeShapeType.MAPLE
    ),
    TreeSpecies(
        id = "weeping_willow",
        name = "Weeping Willow",
        description = "Graceful cascading vines swaying softly beside quiet streams.",
        rarity = Rarity.UNCOMMON,
        price = 220,
        unlockLevel = 2,
        foliageColor = 0xFF558B2F,
        foliageAccentColor = 0xFF8BC34A,
        trunkColor = 0xFF5D4037,
        shapeType = TreeShapeType.WEEPING_WILLOW
    ),
    TreeSpecies(
        id = "sakura_blossom",
        name = "Cherry Blossom",
        description = "Delicate pink petals that dance in the spring wind. Inspires calm serenity.",
        rarity = Rarity.RARE,
        price = 300,
        unlockLevel = 3,
        foliageColor = 0xFFF48FB1,
        foliageAccentColor = 0xFFF8BBD0,
        trunkColor = 0xFF5D4037,
        shapeType = TreeShapeType.SAKURA
    ),
    TreeSpecies(
        id = "jacaranda_bloom",
        name = "Jacaranda Bloom",
        description = "Breathtaking violet-lavender floral canopy radiating creative flow and peaceful harmony.",
        rarity = Rarity.RARE,
        price = 380,
        unlockLevel = 3,
        foliageColor = 0xFF9575CD,
        foliageAccentColor = 0xFFB39DDB,
        trunkColor = 0xFF4E342E,
        shapeType = TreeShapeType.JACARANDA
    ),
    TreeSpecies(
        id = "golden_ginkgo",
        name = "Golden Ginkgo",
        description = "Ancient sacred fan-shaped leaves that turn pure radiant gold in autumn.",
        rarity = Rarity.RARE,
        price = 450,
        unlockLevel = 4,
        foliageColor = 0xFFFFD54F,
        foliageAccentColor = 0xFFFFEE58,
        trunkColor = 0xFF4E342E,
        shapeType = TreeShapeType.GINKGO
    ),
    TreeSpecies(
        id = "ancient_baobab",
        name = "African Baobab",
        description = "Massive sculptural tree of life standing against expansive savannah sunsets.",
        rarity = Rarity.EPIC,
        price = 550,
        unlockLevel = 4,
        foliageColor = 0xFF689F38,
        foliageAccentColor = 0xFF8BC34A,
        trunkColor = 0xFF6D4C41,
        shapeType = TreeShapeType.BAOBAB
    ),
    TreeSpecies(
        id = "zen_bonsai",
        name = "Zen Bonsai",
        description = "Miniature masterwork pruned over decades with patient devotion.",
        rarity = Rarity.EPIC,
        price = 600,
        unlockLevel = 5,
        foliageColor = 0xFF2E7D32,
        foliageAccentColor = 0xFF81C784,
        trunkColor = 0xFF3E2723,
        shapeType = TreeShapeType.BONSAI
    ),
    TreeSpecies(
        id = "giant_sequoia",
        name = "Giant Sequoia",
        description = "Towering ancient titan with cinnamon fibrous bark, reaching toward alpine skies.",
        rarity = Rarity.EPIC,
        price = 750,
        unlockLevel = 6,
        foliageColor = 0xFF1B5E20,
        foliageAccentColor = 0xFF2E7D32,
        trunkColor = 0xFF8D6E63,
        shapeType = TreeShapeType.REDWOOD
    ),
    TreeSpecies(
        id = "moonlit_wisteria",
        name = "Moonlit Wisteria",
        description = "Ethereal cascading lilac and indigo racemes glowing under twilight stars.",
        rarity = Rarity.LEGENDARY,
        price = 1200,
        unlockLevel = 7,
        foliageColor = 0xFF7E57C2,
        foliageAccentColor = 0xFFD1C4E9,
        trunkColor = 0xFF37474F,
        shapeType = TreeShapeType.WISTERIA
    ),
    TreeSpecies(
        id = "celestial_tree",
        name = "Starlight Birch",
        description = "Mythical luminous branches glistening with stardust and lunar glow.",
        rarity = Rarity.LEGENDARY,
        price = 1000,
        unlockLevel = 7,
        foliageColor = 0xFF80DEEA,
        foliageAccentColor = 0xFFB2EBF2,
        trunkColor = 0xFFCFD8DC,
        shapeType = TreeShapeType.CELESTIAL
    ),
    TreeSpecies(
        id = "prismatic_crystal",
        name = "Crystal Prism",
        description = "Enchanted geometric crystal tree that refracts pure focus into rainbow flares.",
        rarity = Rarity.LEGENDARY,
        price = 1500,
        unlockLevel = 8,
        foliageColor = 0xFF80CBC4,
        foliageAccentColor = 0xFFE0F7FA,
        trunkColor = 0xFF90A4AE,
        shapeType = TreeShapeType.CRYSTAL_TREE
    ),
    TreeSpecies(
        id = "aurora_spruce",
        name = "Aurora Spruce",
        description = "Subarctic evergreen kissed by the Northern Lights, shimmering with neon emerald and cyan.",
        rarity = Rarity.SEASONAL,
        price = 850,
        unlockLevel = 4,
        foliageColor = 0xFF00BFA5,
        foliageAccentColor = 0xFF64FFDA,
        trunkColor = 0xFF263238,
        shapeType = TreeShapeType.PINE
    )
)

