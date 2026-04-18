package dev.wceng.sufei.ui.screens.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.wceng.sufei.ui.components.SquareScreenTemplate

@Composable
fun SquareScreen(
    onBackClick: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: SquareViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (val state = uiState) {
            is SquareUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is SquareUiState.Success -> {
                SquareScreenTemplate(
                    title = state.title,
                    items = state.items,
                    onBackClick = onBackClick,
                    onItemClick = onItemClick
                )
            }
            is SquareUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
