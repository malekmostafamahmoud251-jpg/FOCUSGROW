package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.TreeGrowthStage
import com.example.data.model.TreeShapeType
import com.example.data.model.TreeSpecies
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TreeCanvas(
    species: TreeSpecies,
    growthProgress: Float, // 0.0f to 1.0f
    isWithered: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val stage = TreeGrowthStage.fromProgress(growthProgress)

    // Infinite breathing and sway animation
    val infiniteTransition = rememberInfiniteTransition(label = "tree_sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height
            val centerX = width / 2f
            val groundY = height * 0.82f

            // Soft aura behind mature or healthy trees
            if (!isWithered && growthProgress > 0.4f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(species.foliageColor).copy(alpha = 0.18f * auraPulse),
                            Color(species.foliageAccentColor).copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, height * 0.45f),
                        radius = width * 0.48f * auraPulse
                    )
                )
            }

            // Draw Ground Mound
            val groundPath = Path().apply {
                moveTo(centerX - width * 0.38f, groundY + height * 0.04f)
                quadraticTo(
                    centerX, groundY - height * 0.03f,
                    centerX + width * 0.38f, groundY + height * 0.04f
                )
                lineTo(centerX + width * 0.38f, height)
                lineTo(centerX - width * 0.38f, height)
                close()
            }
            drawPath(
                path = groundPath,
                color = if (isWithered) Color(0xFF6D6059) else Color(0xFF4A6B53)
            )

            // Ground Grass / details
            if (!isWithered) {
                drawCircle(
                    color = Color(0xFF5D8566),
                    radius = width * 0.04f,
                    center = Offset(centerX - width * 0.22f, groundY + height * 0.02f)
                )
                drawCircle(
                    color = Color(0xFF5D8566),
                    radius = width * 0.035f,
                    center = Offset(centerX + width * 0.24f, groundY + height * 0.025f)
                )
            }

            if (isWithered) {
                drawWitheredTree(centerX, groundY, width, height, swayAngle)
                return@Canvas
            }

            when (stage) {
                TreeGrowthStage.SEED -> {
                    drawSeedStage(centerX, groundY, width, height, growthProgress)
                }
                TreeGrowthStage.SPROUT -> {
                    drawSproutStage(centerX, groundY, width, height, growthProgress, swayAngle)
                }
                TreeGrowthStage.SAPLING -> {
                    drawSaplingStage(centerX, groundY, width, height, species, growthProgress, swayAngle)
                }
                TreeGrowthStage.YOUNG_TREE -> {
                    drawYoungTreeStage(centerX, groundY, width, height, species, growthProgress, swayAngle)
                }
                TreeGrowthStage.MATURE_TREE -> {
                    drawMatureTreeStage(centerX, groundY, width, height, species, swayAngle)
                }
            }
        }
    }
}

private fun DrawScope.drawSeedStage(
    cx: Float, gy: Float, w: Float, h: Float, progress: Float
) {
    val seedRadius = w * 0.045f
    val seedCenter = Offset(cx, gy - seedRadius * 0.8f)

    // Golden seed shell
    drawOval(
        color = Color(0xFF8D6E63),
        topLeft = Offset(seedCenter.x - seedRadius * 0.8f, seedCenter.y - seedRadius),
        size = Size(seedRadius * 1.6f, seedRadius * 2f)
    )

    // Tiny sprout tip starting to poke out
    val sproutHeight = (progress / 0.2f).coerceIn(0f, 1f) * h * 0.05f
    if (sproutHeight > 0f) {
        val sproutPath = Path().apply {
            moveTo(cx, seedCenter.y - seedRadius)
            quadraticTo(
                cx + w * 0.02f, seedCenter.y - seedRadius - sproutHeight * 0.6f,
                cx + w * 0.035f, seedCenter.y - seedRadius - sproutHeight
            )
            quadraticTo(
                cx + w * 0.01f, seedCenter.y - seedRadius - sproutHeight * 0.7f,
                cx, seedCenter.y - seedRadius
            )
            close()
        }
        drawPath(sproutPath, Color(0xFF81C784))
    }
}

private fun DrawScope.drawSproutStage(
    cx: Float, gy: Float, w: Float, h: Float, progress: Float, sway: Float
) {
    val stemProgress = ((progress - 0.1f) / 0.25f).coerceIn(0.2f, 1f)
    val stemHeight = h * 0.16f * stemProgress
    val topX = cx + sin(Math.toRadians(sway.toDouble())).toFloat() * w * 0.04f
    val topY = gy - stemHeight

    // Stem
    val stemPath = Path().apply {
        moveTo(cx - w * 0.012f, gy)
        quadraticTo(cx, gy - stemHeight * 0.5f, topX, topY)
        lineTo(topX + w * 0.015f, topY)
        quadraticTo(cx + w * 0.012f, gy - stemHeight * 0.5f, cx + w * 0.012f, gy)
        close()
    }
    drawPath(stemPath, Color(0xFF66BB6A))

    // Left Leaf
    val leafSize = w * 0.07f * stemProgress
    val leftLeaf = Path().apply {
        moveTo(topX, topY)
        cubicTo(
            topX - leafSize * 1.2f, topY - leafSize * 0.4f,
            topX - leafSize * 1.4f, topY + leafSize * 0.6f,
            topX, topY + leafSize * 0.3f
        )
        close()
    }
    drawPath(leftLeaf, Color(0xFF81C784))

    // Right Leaf
    val rightLeaf = Path().apply {
        moveTo(topX, topY - leafSize * 0.1f)
        cubicTo(
            topX + leafSize * 1.2f, topY - leafSize * 0.5f,
            topX + leafSize * 1.4f, topY + leafSize * 0.5f,
            topX, topY + leafSize * 0.2f
        )
        close()
    }
    drawPath(rightLeaf, Color(0xFF4CAF50))
}

private fun DrawScope.drawSaplingStage(
    cx: Float, gy: Float, w: Float, h: Float, species: TreeSpecies, progress: Float, sway: Float
) {
    val stageProg = ((progress - 0.35f) / 0.3f).coerceIn(0.2f, 1f)
    val trunkHeight = h * 0.32f * stageProg
    val topX = cx + sin(Math.toRadians(sway.toDouble())).toFloat() * w * 0.06f
    val topY = gy - trunkHeight

    // Trunk
    val trunkPath = Path().apply {
        moveTo(cx - w * 0.025f, gy)
        lineTo(cx + w * 0.025f, gy)
        lineTo(topX + w * 0.012f, topY)
        lineTo(topX - w * 0.012f, topY)
        close()
    }
    drawPath(trunkPath, Color(species.trunkColor))

    // Young Foliage Clusters
    val foliageR = w * 0.14f * stageProg
    drawCircle(
        color = Color(species.foliageColor),
        radius = foliageR,
        center = Offset(topX, topY - foliageR * 0.3f)
    )
    drawCircle(
        color = Color(species.foliageAccentColor),
        radius = foliageR * 0.75f,
        center = Offset(topX - foliageR * 0.4f, topY - foliageR * 0.4f)
    )
    drawCircle(
        color = Color(species.foliageColor).copy(alpha = 0.9f),
        radius = foliageR * 0.65f,
        center = Offset(topX + foliageR * 0.45f, topY - foliageR * 0.2f)
    )
}

private fun DrawScope.drawYoungTreeStage(
    cx: Float, gy: Float, w: Float, h: Float, species: TreeSpecies, progress: Float, sway: Float
) {
    if (species.shapeType == TreeShapeType.BAMBOO) {
        drawBambooGrove(cx, gy, w * 0.82f, h * 0.82f, species, sway)
        return
    }

    val trunkHeight = h * 0.46f
    val topX = cx + sin(Math.toRadians(sway.toDouble())).toFloat() * w * 0.08f
    val topY = gy - trunkHeight

    // Strong trunk with slight flare
    val trunkPath = Path().apply {
        moveTo(cx - w * 0.045f, gy)
        quadraticTo(cx - w * 0.03f, gy - trunkHeight * 0.5f, topX - w * 0.02f, topY)
        lineTo(topX + w * 0.02f, topY)
        quadraticTo(cx + w * 0.03f, gy - trunkHeight * 0.5f, cx + w * 0.045f, gy)
        close()
    }
    drawPath(trunkPath, Color(species.trunkColor))

    // Shape specific rendering for young stage
    drawCanopy(topX, topY, w * 0.85f, h * 0.85f, species, sway)
}

private fun DrawScope.drawMatureTreeStage(
    cx: Float, gy: Float, w: Float, h: Float, species: TreeSpecies, sway: Float
) {
    if (species.shapeType == TreeShapeType.BAMBOO) {
        drawBambooGrove(cx, gy, w, h, species, sway)
        return
    }

    val trunkHeight = when (species.shapeType) {
        TreeShapeType.REDWOOD -> h * 0.62f
        TreeShapeType.BAOBAB -> h * 0.44f
        else -> h * 0.52f
    }
    val topX = cx + sin(Math.toRadians(sway.toDouble())).toFloat() * w * 0.07f
    val topY = gy - trunkHeight

    if (species.shapeType == TreeShapeType.BAOBAB) {
        // Massive, sculpted African baobab trunk
        val trunkBaseW = w * 0.16f
        val trunkTopW = w * 0.12f
        val trunkPath = Path().apply {
            moveTo(cx - trunkBaseW, gy)
            quadraticTo(cx - trunkBaseW * 0.85f, gy - trunkHeight * 0.5f, topX - trunkTopW, topY)
            lineTo(topX + trunkTopW, topY)
            quadraticTo(cx + trunkBaseW * 0.85f, gy - trunkHeight * 0.5f, cx + trunkBaseW, gy)
            close()
        }
        drawPath(trunkPath, Color(species.trunkColor))
        // Bark creases
        for (i in -1..1) {
            val lineX = cx + i * w * 0.05f
            drawLine(
                color = Color(species.trunkColor).copy(alpha = 0.5f),
                start = Offset(lineX, gy - trunkHeight * 0.15f),
                end = Offset(topX + i * w * 0.035f, topY + trunkHeight * 0.15f),
                strokeWidth = 3f
            )
        }
    } else if (species.shapeType == TreeShapeType.CRYSTAL_TREE) {
        // Faceted crystal trunk
        val trunkPath = Path().apply {
            moveTo(cx - w * 0.05f, gy)
            lineTo(cx - w * 0.02f, gy - trunkHeight * 0.6f)
            lineTo(topX - w * 0.015f, topY)
            lineTo(topX + w * 0.015f, topY)
            lineTo(cx + w * 0.02f, gy - trunkHeight * 0.6f)
            lineTo(cx + w * 0.05f, gy)
            close()
        }
        drawPath(trunkPath, Color(species.trunkColor))
        // Crystal facet line
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(cx, gy),
            end = Offset(topX, topY),
            strokeWidth = 2.5f
        )
    } else {
        // Sturdy Ancient Trunk
        val trunkBaseWidth = if (species.shapeType == TreeShapeType.REDWOOD) w * 0.08f else w * 0.065f
        val trunkPath = Path().apply {
            moveTo(cx - trunkBaseWidth, gy)
            quadraticTo(cx - trunkBaseWidth * 0.6f, gy - trunkHeight * 0.4f, topX - w * 0.028f, topY)
            lineTo(topX + w * 0.028f, topY)
            quadraticTo(cx + trunkBaseWidth * 0.6f, gy - trunkHeight * 0.4f, cx + trunkBaseWidth, gy)
            close()
        }
        drawPath(trunkPath, Color(species.trunkColor))

        // Roots flare
        drawCircle(
            color = Color(species.trunkColor),
            radius = w * 0.04f,
            center = Offset(cx - trunkBaseWidth * 1.1f, gy + h * 0.01f)
        )
        drawCircle(
            color = Color(species.trunkColor),
            radius = w * 0.035f,
            center = Offset(cx + trunkBaseWidth * 1.1f, gy + h * 0.01f)
        )
    }

    // Full Canopy
    drawCanopy(topX, topY, w, h, species, sway)
}

private fun DrawScope.drawCanopy(
    topX: Float, topY: Float, w: Float, h: Float, species: TreeSpecies, sway: Float
) {
    when (species.shapeType) {
        TreeShapeType.PINE -> {
            // Triangular multi-tiered pine needle layers
            val layers = 4
            val tierHeight = h * 0.12f
            for (i in 0 until layers) {
                val tierWidth = (w * 0.22f) + (i * w * 0.10f)
                val tierY = topY - (layers - 1 - i) * tierHeight * 0.75f

                val pineTier = Path().apply {
                    moveTo(topX, tierY - tierHeight)
                    lineTo(topX + tierWidth, tierY + tierHeight * 0.6f)
                    quadraticTo(topX, tierY + tierHeight * 0.35f, topX - tierWidth, tierY + tierHeight * 0.6f)
                    close()
                }
                val tierColor = if (i % 2 == 0) Color(species.foliageColor) else Color(species.foliageAccentColor)
                drawPath(pineTier, tierColor)
            }
        }
        TreeShapeType.ROUND_OAK, TreeShapeType.MAPLE, TreeShapeType.GINKGO -> {
            // Lush billowing clouds of leaves
            val baseRadius = w * 0.22f
            val clusters = listOf(
                Offset(0f, -baseRadius * 0.6f) to baseRadius * 1.15f,
                Offset(-baseRadius * 0.8f, -baseRadius * 0.2f) to baseRadius * 0.95f,
                Offset(baseRadius * 0.8f, -baseRadius * 0.2f) to baseRadius * 0.95f,
                Offset(-baseRadius * 0.5f, -baseRadius * 1.1f) to baseRadius * 0.9f,
                Offset(baseRadius * 0.5f, -baseRadius * 1.1f) to baseRadius * 0.9f,
                Offset(0f, -baseRadius * 1.4f) to baseRadius * 0.8f
            )
            clusters.forEachIndexed { index, pair ->
                val center = Offset(topX + pair.first.x, topY + pair.first.y)
                val color = if (index % 2 == 0) Color(species.foliageColor) else Color(species.foliageAccentColor)
                drawCircle(color = color, radius = pair.second, center = center)
            }
        }
        TreeShapeType.SAKURA -> {
            // Soft blossoming pink clusters with drifting petals
            val baseRadius = w * 0.21f
            val clusters = listOf(
                Offset(0f, -baseRadius * 0.7f) to baseRadius * 1.1f,
                Offset(-baseRadius * 0.85f, -baseRadius * 0.3f) to baseRadius * 0.9f,
                Offset(baseRadius * 0.85f, -baseRadius * 0.3f) to baseRadius * 0.9f,
                Offset(-baseRadius * 0.4f, -baseRadius * 1.2f) to baseRadius * 0.85f,
                Offset(baseRadius * 0.4f, -baseRadius * 1.2f) to baseRadius * 0.85f
            )
            clusters.forEachIndexed { idx, pair ->
                val center = Offset(topX + pair.first.x, topY + pair.first.y)
                val color = if (idx % 2 == 0) Color(species.foliageColor) else Color(species.foliageAccentColor)
                drawCircle(color = color, radius = pair.second, center = center)
            }
            // Tiny blossom highlights
            drawCircle(Color.White.copy(alpha = 0.8f), baseRadius * 0.12f, Offset(topX - baseRadius * 0.4f, topY - baseRadius * 0.6f))
            drawCircle(Color.White.copy(alpha = 0.8f), baseRadius * 0.10f, Offset(topX + baseRadius * 0.5f, topY - baseRadius * 0.8f))
        }
        TreeShapeType.JACARANDA -> {
            // Dreamy violet-lavender floral canopy with floating petal specks
            val baseRadius = w * 0.23f
            val clusters = listOf(
                Offset(0f, -baseRadius * 0.7f) to baseRadius * 1.15f,
                Offset(-baseRadius * 0.9f, -baseRadius * 0.3f) to baseRadius * 0.95f,
                Offset(baseRadius * 0.9f, -baseRadius * 0.3f) to baseRadius * 0.95f,
                Offset(-baseRadius * 0.45f, -baseRadius * 1.25f) to baseRadius * 0.9f,
                Offset(baseRadius * 0.45f, -baseRadius * 1.25f) to baseRadius * 0.9f,
                Offset(0f, -baseRadius * 1.5f) to baseRadius * 0.8f
            )
            clusters.forEachIndexed { idx, pair ->
                val center = Offset(topX + pair.first.x, topY + pair.first.y)
                val color = if (idx % 2 == 0) Color(species.foliageColor) else Color(species.foliageAccentColor)
                drawCircle(color = color, radius = pair.second, center = center)
            }
            // Lilac blossom highlights
            drawCircle(Color.White.copy(alpha = 0.75f), baseRadius * 0.12f, Offset(topX - baseRadius * 0.3f, topY - baseRadius * 0.8f))
            drawCircle(Color.White.copy(alpha = 0.75f), baseRadius * 0.10f, Offset(topX + baseRadius * 0.4f, topY - baseRadius * 0.5f))
        }
        TreeShapeType.BAOBAB -> {
            // Sprawling thick branches with dense round canopies on branch ends
            val branchLeft = Offset(topX - w * 0.22f, topY - h * 0.06f)
            val branchRight = Offset(topX + w * 0.22f, topY - h * 0.06f)
            val branchCenter = Offset(topX, topY - h * 0.12f)

            // Major limbs
            drawLine(Color(species.trunkColor), Offset(topX, topY), branchLeft, strokeWidth = 14f, cap = StrokeCap.Round)
            drawLine(Color(species.trunkColor), Offset(topX, topY), branchRight, strokeWidth = 14f, cap = StrokeCap.Round)
            drawLine(Color(species.trunkColor), Offset(topX, topY), branchCenter, strokeWidth = 16f, cap = StrokeCap.Round)

            // Foliage caps
            val capR = w * 0.13f
            drawCircle(Color(species.foliageColor), capR * 1.1f, branchLeft)
            drawCircle(Color(species.foliageAccentColor), capR * 0.85f, Offset(branchLeft.x - capR * 0.2f, branchLeft.y - capR * 0.2f))

            drawCircle(Color(species.foliageColor), capR * 1.1f, branchRight)
            drawCircle(Color(species.foliageAccentColor), capR * 0.85f, Offset(branchRight.x + capR * 0.2f, branchRight.y - capR * 0.2f))

            drawCircle(Color(species.foliageColor), capR * 1.3f, branchCenter)
            drawCircle(Color(species.foliageAccentColor), capR * 1.0f, Offset(branchCenter.x, branchCenter.y - capR * 0.3f))
        }
        TreeShapeType.REDWOOD -> {
            // Towering layered evergreen tiers with horizontal branch spreads
            val tiers = 6
            val tierH = h * 0.08f
            for (i in 0 until tiers) {
                val tierW = (w * 0.10f) + (i * w * 0.045f)
                val tierY = topY - (tiers - 1 - i) * tierH * 0.85f
                val path = Path().apply {
                    moveTo(topX, tierY - tierH)
                    lineTo(topX + tierW, tierY + tierH * 0.5f)
                    quadraticTo(topX, tierY + tierH * 0.2f, topX - tierW, tierY + tierH * 0.5f)
                    close()
                }
                val col = if (i % 2 == 0) Color(species.foliageColor) else Color(species.foliageAccentColor)
                drawPath(path, col)
            }
        }
        TreeShapeType.WISTERIA -> {
            // Hanging pendulous lilac flower racemes
            val baseR = w * 0.24f
            drawCircle(Color(species.foliageColor).copy(alpha = 0.85f), baseR * 0.9f, Offset(topX, topY - baseR * 0.6f))
            // Draped floral vines
            for (i in -5..5) {
                val vineX = topX + (i * w * 0.045f)
                val dropLength = h * (0.16f + (kotlin.math.abs(i) % 3) * 0.04f)
                val vinePath = Path().apply {
                    moveTo(vineX, topY - baseR * 0.3f)
                    quadraticTo(
                        vineX + sin((i + sway * 1.2f).toDouble()).toFloat() * 10f,
                        topY + dropLength * 0.5f,
                        vineX + (i * 1.5f),
                        topY + dropLength
                    )
                }
                drawPath(vinePath, Color(species.foliageAccentColor), style = Stroke(width = 6f, cap = StrokeCap.Round))
                // Hanging blossom tip
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = 4f,
                    center = Offset(vineX + (i * 1.5f), topY + dropLength)
                )
            }
        }
        TreeShapeType.CRYSTAL_TREE -> {
            // Faceted gem crystal cluster with rainbow refraction
            val r = w * 0.26f
            val crystalCenter = Offset(topX, topY - r * 0.7f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(species.foliageAccentColor),
                        Color(species.foliageColor),
                        Color(0xFF004D40).copy(alpha = 0.3f)
                    ),
                    center = crystalCenter,
                    radius = r * 1.2f
                ),
                radius = r,
                center = crystalCenter
            )
            // Prismatic crystal shard triangles
            val shards = listOf(
                Offset(0f, -r * 1.1f),
                Offset(-r * 0.65f, -r * 0.4f),
                Offset(r * 0.65f, -r * 0.4f),
                Offset(-r * 0.35f, -r * 0.9f),
                Offset(r * 0.35f, -r * 0.9f)
            )
            shards.forEach { tip ->
                val shardPath = Path().apply {
                    moveTo(crystalCenter.x + tip.x, crystalCenter.y + tip.y)
                    lineTo(crystalCenter.x + tip.x * 0.6f + 10f, crystalCenter.y + tip.y * 0.5f)
                    lineTo(crystalCenter.x + tip.x * 0.6f - 10f, crystalCenter.y + tip.y * 0.5f)
                    close()
                }
                drawPath(shardPath, Color.White.copy(alpha = 0.6f))
            }
            // Specular diamond stars
            drawCircle(Color.White, 7f, Offset(crystalCenter.x - r * 0.3f, crystalCenter.y - r * 0.4f))
            drawCircle(Color.White, 5f, Offset(crystalCenter.x + r * 0.4f, crystalCenter.y - r * 0.2f))
            drawCircle(Color.White, 8f, Offset(crystalCenter.x, crystalCenter.y - r * 1.0f))
        }
        TreeShapeType.WEEPING_WILLOW -> {
            // Cascading graceful curtain vines
            val baseR = w * 0.26f
            drawCircle(Color(species.foliageColor), baseR, Offset(topX, topY - baseR * 0.8f))
            // Vines
            for (i in -4..4) {
                val vineX = topX + (i * w * 0.055f)
                val vinePath = Path().apply {
                    moveTo(vineX, topY - baseR * 0.6f)
                    quadraticTo(
                        vineX + sin((i + sway).toDouble()).toFloat() * 12f,
                        topY + h * 0.1f,
                        vineX + (i * 2f),
                        topY + h * 0.22f
                    )
                }
                drawPath(vinePath, Color(species.foliageAccentColor), style = Stroke(width = 4f))
            }
        }
        TreeShapeType.BONSAI -> {
            // Curving Japanese bonsai with dense mossy pads
            val padR = w * 0.16f
            drawCircle(Color(species.foliageColor), padR * 1.1f, Offset(topX - w * 0.18f, topY - h * 0.05f))
            drawCircle(Color(species.foliageAccentColor), padR * 0.9f, Offset(topX + w * 0.16f, topY - h * 0.12f))
            drawCircle(Color(species.foliageColor), padR * 1.2f, Offset(topX, topY - h * 0.22f))
        }
        TreeShapeType.PALM -> {
            // Tropical spreading fronds
            for (i in 0 until 6) {
                val angle = (i * 60f) + (sway * 1.5f)
                val rad = Math.toRadians(angle.toDouble())
                val frondEndX = topX + (cos(rad) * w * 0.32f).toFloat()
                val frondEndY = (topY - h * 0.05f) + (sin(rad) * h * 0.18f).toFloat()

                val frond = Path().apply {
                    moveTo(topX, topY - h * 0.05f)
                    quadraticTo(topX + (frondEndX - topX) * 0.5f, topY - h * 0.15f, frondEndX, frondEndY)
                }
                drawPath(frond, Color(species.foliageColor), style = Stroke(width = 9f, cap = StrokeCap.Round))
            }
        }
        TreeShapeType.CELESTIAL -> {
            // Glowing magical star crown
            val radius = w * 0.28f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF00ACC1).copy(alpha = 0.4f)),
                    center = Offset(topX, topY - radius * 0.7f),
                    radius = radius * 1.2f
                ),
                radius = radius,
                center = Offset(topX, topY - radius * 0.7f)
            )
            // Starlight glints
            drawCircle(Color.White, 7f, Offset(topX - radius * 0.4f, topY - radius * 0.8f))
            drawCircle(Color.White, 5f, Offset(topX + radius * 0.35f, topY - radius * 0.4f))
            drawCircle(Color.White, 8f, Offset(topX, topY - radius * 1.2f))
        }
        TreeShapeType.BAMBOO -> {
            // Handled in drawBambooGrove
        }
    }
}

private fun DrawScope.drawBambooGrove(
    cx: Float, gy: Float, w: Float, h: Float, species: TreeSpecies, sway: Float
) {
    val culmCount = 5
    for (idx in 0 until culmCount) {
        val culmOffsetX = (idx - 2) * (w * 0.075f)
        val culmHeight = h * (0.46f + (idx % 3) * 0.06f)
        val culmTopX = cx + culmOffsetX + sin(Math.toRadians((sway * (1f + idx * 0.2f)).toDouble())).toFloat() * (w * 0.045f)
        val culmTopY = gy - culmHeight
        val culmWidth = w * 0.022f

        // Draw bamboo culm segments
        val segments = 5
        val segHeight = culmHeight / segments
        for (s in 0 until segments) {
            val segBottomY = gy - (s * segHeight)
            val segTopY = gy - ((s + 1) * segHeight)
            val ratio = (s + 0.5f) / segments
            val segX = cx + culmOffsetX + (culmTopX - (cx + culmOffsetX)) * ratio

            drawLine(
                color = Color(species.trunkColor),
                start = Offset(segX, segBottomY),
                end = Offset(segX, segTopY + 2f),
                strokeWidth = culmWidth,
                cap = StrokeCap.Round
            )
            // Node ring
            drawLine(
                color = Color(species.foliageAccentColor),
                start = Offset(segX - culmWidth * 0.8f, segTopY),
                end = Offset(segX + culmWidth * 0.8f, segTopY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Delicate leaf spray from nodes
            if (s >= 2) {
                val leafDir = if ((s + idx) % 2 == 0) 1f else -1f
                val leafLength = w * 0.09f
                val leafPath = Path().apply {
                    moveTo(segX, segTopY)
                    quadraticTo(
                        segX + leafDir * leafLength * 0.7f, segTopY - 8f,
                        segX + leafDir * leafLength, segTopY + 4f
                    )
                    quadraticTo(
                        segX + leafDir * leafLength * 0.5f, segTopY + 2f,
                        segX, segTopY
                    )
                    close()
                }
                drawPath(leafPath, Color(species.foliageColor))
            }
        }
    }
}

private fun DrawScope.drawWitheredTree(
    cx: Float, gy: Float, w: Float, h: Float, sway: Float
) {
    val trunkHeight = h * 0.48f
    val topX = cx
    val topY = gy - trunkHeight

    // Dry withered trunk
    val trunk = Path().apply {
        moveTo(cx - w * 0.045f, gy)
        lineTo(topX - w * 0.02f, topY)
        lineTo(topX + w * 0.02f, topY)
        lineTo(cx + w * 0.045f, gy)
        close()
    }
    drawPath(trunk, Color(0xFF6D6059))

    // Twisted bare dead branches
    val b1 = Path().apply {
        moveTo(topX, topY + trunkHeight * 0.2f)
        quadraticTo(topX - w * 0.12f, topY, topX - w * 0.22f, topY - h * 0.05f)
        lineTo(topX - w * 0.26f, topY - h * 0.08f)
    }
    drawPath(b1, Color(0xFF6D6059), style = Stroke(width = 6f, cap = StrokeCap.Round))

    val b2 = Path().apply {
        moveTo(topX, topY + trunkHeight * 0.4f)
        quadraticTo(topX + w * 0.12f, topY + trunkHeight * 0.1f, topX + w * 0.24f, topY)
    }
    drawPath(b2, Color(0xFF6D6059), style = Stroke(width = 5f, cap = StrokeCap.Round))

    val b3 = Path().apply {
        moveTo(topX, topY)
        lineTo(topX - w * 0.08f, topY - h * 0.12f)
    }
    drawPath(b3, Color(0xFF6D6059), style = Stroke(width = 4f, cap = StrokeCap.Round))

    // Fallen withered leaves on the ground
    drawCircle(Color(0xFF8D6E63), 6f, Offset(cx - w * 0.14f, gy + h * 0.02f))
    drawCircle(Color(0xFF8D6E63), 5f, Offset(cx + w * 0.16f, gy + h * 0.015f))
    drawCircle(Color(0xFF6D6059), 7f, Offset(cx - w * 0.04f, gy + h * 0.03f))
}
