import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.infusion.ui.theme.MainViewModel
import com.example.infusion.ui.theme.ModelContainer

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val models by viewModel.models.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    val availableModels = listOf(
        "Microscope.glb",
        "SolarSystem.glb",
        "Bulb.glb",
        "Fiagena.glb",
        "Lungs.glb"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        models.forEach { model ->
            key(model.id) {
                ModelContainer(
                    modelItem = model,
                    onDelete = { viewModel.removeModel(model.id) },
                    onUpdate = { viewModel.updateModel(it) }
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { showDialog = true }
        ) {
            Text("Add Model")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select a Model") },
            text = {
                Column {
                    availableModels.forEach { fileName ->
                        TextButton(onClick = {
                            viewModel.addModel(fileName)
                            showDialog = false
                        }) {
                            Text(fileName.replace(".glb", ""))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}