package com.example.infusion.ui.theme

import androidx.lifecycle.ViewModel
import com.example.infusion.model.ModelItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class MainViewModel : ViewModel() {

    private val _models = MutableStateFlow<List<ModelItem>>(emptyList())
    val models: StateFlow<List<ModelItem>>get() = _models

    fun addModel(fileName: String) {
        val model = ModelItem(
            id = UUID.randomUUID().toString(),
            modelFile = fileName
        )

        _models.update {
            it + model
        }
    }

    fun removeModel(id : String) {
        _models.update {
            it.filterNot { it.id == id }
        }
    }

    fun updateModel(updatedModel: ModelItem) {
        _models.update { list ->
            list.map {
                if(it.id == updatedModel.id) {
                    updatedModel
                }
                else
                    it
            }
        }
    }

}