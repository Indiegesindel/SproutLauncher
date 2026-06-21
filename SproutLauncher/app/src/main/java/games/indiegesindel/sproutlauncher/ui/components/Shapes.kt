package games.indiegesindel.sproutlauncher.ui.components

import androidx.compose.foundation.shape.GenericShape

val TriangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, size.height)
    lineTo(0f, 0f)
    lineTo(size.width, 0f)
    close()
}
