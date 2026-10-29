package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun QrisCanvas(
    payload: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    // Deterministic matrix derived from the payload hash
    val matrixSize = 25
    val modules = remember(payload) {
        val seed = payload.hashCode().toLong()
        val rng = Random(seed)
        val mat = Array(matrixSize) { BooleanArray(matrixSize) }

        // Fill with pseudo-random pattern
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                mat[r][c] = rng.nextBoolean()
            }
        }

        // Draw 3 standard QR Finder Patterns (7x7 at (0,0), (0, 18), (18, 0))
        fun placeFinder(startR: Int, startC: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    mat[startR + r][startC + c] = isBorder || isCenter
                }
            }
            // Clear separators around finder
            for (r in -1..7) {
                val checkR = startR + r
                if (checkR in 0 until matrixSize) {
                    val cBefore = startC - 1
                    val cAfter = startC + 7
                    if (cBefore in 0 until matrixSize) mat[checkR][cBefore] = false
                    if (cAfter in 0 until matrixSize) mat[checkR][cAfter] = false
                }
            }
            for (c in -1..7) {
                val checkC = startC + c
                if (checkC in 0 until matrixSize) {
                    val rBefore = startR - 1
                    val rAfter = startR + 7
                    if (rBefore in 0 until matrixSize) mat[rBefore][checkC] = false
                    if (rAfter in 0 until matrixSize) mat[rAfter][checkC] = false
                }
            }
        }

        placeFinder(0, 0)
        placeFinder(0, matrixSize - 7)
        placeFinder(matrixSize - 7, 0)

        // Clear center for QRIS badge (e.g. 9x9 center)
        val centerStart = (matrixSize - 5) / 2
        for (r in centerStart until centerStart + 5) {
            for (c in centerStart until centerStart + 5) {
                mat[r][c] = false
            }
        }

        mat
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val stepX = this.size.width / matrixSize
            val stepY = this.size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (modules[r][c]) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(c * stepX, r * stepY),
                            size = Size(stepX + 0.5f, stepY + 0.5f)
                        )
                    }
                }
            }
        }

        // QRIS Center Badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEE2737))
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QRIS",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun BarcodeCanvas(
    code: String,
    modifier: Modifier = Modifier,
    width: Dp = 260.dp,
    height: Dp = 64.dp
) {
    val barPattern = remember(code) {
        val seed = code.hashCode().toLong()
        val rng = Random(seed)
        List(48) { rng.nextInt(1, 4) }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(width = width, height = height - 20.dp)
        ) {
            val totalUnits = barPattern.sum() + barPattern.size
            val unitWidth = size.width / totalUnits
            var currentX = 0f

            barPattern.forEachIndexed { index, barWeight ->
                val barW = barWeight * unitWidth
                if (index % 2 == 0) {
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(currentX, 0f),
                        size = Size(barW, size.height)
                    )
                }
                currentX += barW + unitWidth
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = code,
            color = Color(0xFF334155),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
