package io.github.jisungbin.simpletodocompose

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

@SuppressLint("NewApi")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionInit()
        setContent {
            val tasks = remember {
                mutableStateListOf(*Storage.fileList(PathConfig.AllPath).toTasks().toTypedArray())
            }

            MaterialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Toolbar(onTaskAddAction = { task ->
                            if (task.name.isNotBlank()) {
                                tasks.add(task)
                            }
                        })
                    }
                ) {
                    Content(tasks = tasks, onTaskRemoveAction = { task ->
                        tasks.remove(task)
                    })
                }
            }
        }
    }

    @Composable
    private fun Content(tasks: List<Task>, onTaskRemoveAction: (Task) -> Unit) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskItem(task = task, onTaskRemoveAction = { _task ->
                    onTaskRemoveAction(_task)
                })
            }
        }
    }

    @Composable
    private fun Toolbar(onTaskAddAction: (Task) -> Unit) {
        val taskAddDialogVisible = remember { mutableStateOf(false) }
        TaskAddDialog(
            visible = taskAddDialogVisible,
            onDoneAction = { taskNameField -> // it
                onTaskAddAction(Task(name = taskNameField.text, done = false))
            }
        )

        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.app_name), color = Color.White)
                Icon(
                    painter = painterResource(R.drawable.ic_round_add_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.clickable {
                        taskAddDialogVisible.value = true
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TaskItem(task: Task, onTaskRemoveAction: (Task) -> Unit) {
        var done by remember { mutableStateOf(task.done) }

        Storage.write(PathConfig.Task(task.name), done.toString())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .combinedClickable(
                    onClick = { done = !done },
                    onLongClick = {
                        onTaskRemoveAction(task)
                        Storage.delete(PathConfig.Task(task.name))
                    }
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = done, onCheckedChange = { done = !done })
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = task.name,
                color = if (done) Color.LightGray else Color.Black,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }

    @Composable
    private fun TaskAddDialog(
        visible: MutableState<Boolean>,
        onDoneAction: (TextFieldValue) -> Unit
    ) {
        if (visible.value) {
            var taskNameField by remember { mutableStateOf(TextFieldValue()) }

            AlertDialog(
                onDismissRequest = { visible.value = false },
                title = { Text(text = stringResource(R.string.activity_main_task_add_dialog_title)) },
                confirmButton = {
                    Button(onClick = {
                        onDoneAction(taskNameField)
                        visible.value = false
                    }) {
                        Text(text = stringResource(R.string.activity_main_task_add_dialog_button_add))
                    }
                },
                text = {
                    Column {
                        Text(modifier = Modifier.height(10.dp), text = "")
                        OutlinedTextField(
                            value = taskNameField,
                            onValueChange = { taskNameField = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.activity_main_task_add_dialog_placeholder_task_name),
                                    color = Color.LightGray
                                )
                            }
                        )
                    }
                }
            )
        }
    }

    private fun permissionInit() {
        val storagePermissionGranted = if (Storage.isScoped) {
            Storage.isStorageManagerPermissionGranted()
        } else {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (!storagePermissionGranted) {
            if (Storage.isScoped) {
                Storage.requestStorageManagePermission(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    1000
                )
            }
        }
    }
}
