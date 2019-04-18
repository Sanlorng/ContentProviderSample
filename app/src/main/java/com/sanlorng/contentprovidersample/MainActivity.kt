package com.sanlorng.contentprovidersample

import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_edit_user.view.*

class MainActivity : AppCompatActivity() {

    private val uriString = "content://com.sanlorng.classsample/user"
    private val uri = Uri.parse(uriString)
    private var adapter: UserListAdapter? = null
    private var dialog: AlertDialog? = null
    private var list = ArrayList<UserEntry>()
    private val selectList = HashMap<Long,Long>()
    private var selectMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.parseColor("#ffffff")
        val ui = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setSupportActionBar(toolbarMain)
        loadList()
        swipeMain.setOnRefreshListener {
            loadList()
            swipeMain.isRefreshing = false
        }
        toolbarMain.setNavigationOnClickListener {
            adapter?.selectMode = false
        }
        addAccount.setOnClickListener {
            if (selectMode) {
                MaterialAlertDialogBuilder(this)
                        .setTitle("确认删除")
                        .setMessage("您确认要删除已选择的账号吗?")
                        .setCancelable(true)
                        .setPositiveButton("确定") { _, _ ->
                            selectList.forEach {
                                delete(it.value)
                                adapter?.selectMode = false
                                loadList()
                            }
                        }
                        .create()
                        .show()
            }
            else {
                dialog = MaterialAlertDialogBuilder(this@MainActivity)
                        .setView(View.inflate(this@MainActivity, R.layout.layout_edit_user, null).apply {
                            val listener = {
                                when {
                                    editName.text?.trim()?.isNotEmpty() != true || editName.text?.trim()?.length != 11 -> {
                                        editName.error = "账号格式有误"
                                        editName.requestFocus()
                                        false
                                    }
                                    editPass.text?.trim()?.isNotEmpty() != true || editPass.text?.trim()?.length != 6 -> {
                                        editPass.error = "密码格式有误"
                                        editPass.requestFocus()
                                        false
                                    }
                                    editAge.text?.trim()?.isNotEmpty() != true -> {
                                        editAge.error = "年龄格式有误"
                                        editAge.requestFocus()
                                        false
                                    }
                                    else -> {
                                        val value = ContentValues()
                                        value.put(UserEntry.COULMN_NAME, editName.text?.trim()?.toString()
                                                ?: "")
                                        value.put(UserEntry.COULMN_PASS, editPass.text?.trim()?.toString()
                                                ?: "")
                                        value.put(UserEntry.COULMN_AGE, editAge.text?.trim()?.toString()?.toLong())
                                        insert(value)
                                        dialog?.dismiss()
                                        true
                                    }
                                }


                            }
                            editAge.setOnEditorActionListener { v, actionId, event ->
                                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                                    return@setOnEditorActionListener listener.invoke()
                                }
                                false
                            }
                            editDone.setOnClickListener {
                                listener.invoke()
                            }
                        })
                        .setTitle("创建账号")
                        .create()
                dialog?.show()
            }
        }
    }

    private fun delete(id: Long) {
        contentResolver.delete(Uri.parse("$uriString/$id"), null,null)
    }
    private fun insert(value: ContentValues) {
        contentResolver.insert(uri,value)
//        list.add(UserEntry(value.getAsString(UserEntry.COULMN_NAME),value.getAsString(UserEntry.COULMN_PASS),value.getAsLong(UserEntry.COULMN_AGE)))
//        adapter?.notifyItemInserted(list.lastIndex)
        Log.e("insert",uri.toString())
        loadList()
    }

    private fun update(value: ContentValues,id:Long) {
        contentResolver.update(Uri.parse("$uriString/$id"),value, null,null)
//        adapter?.notifyItemChanged()
        Log.e("update",uri.toString())
        loadList()
    }

    private fun loadList() {
        val cursor = contentResolver.query(uri,null,null,null,null)
        cursor?.apply {
            list.clear()
            while (moveToNext()) {
                list.add(UserEntry(getString(getColumnIndex(UserEntry.COULMN_NAME)),getString(getColumnIndex(UserEntry.COULMN_PASS))).apply {
                    id = getLong(getColumnIndex(UserEntry.COULMN_ID))
                    age = getInt(getColumnIndex(UserEntry.COULMN_AGE))
                })
            }
            listMain.layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = UserListAdapter(list,{
                dialog = MaterialAlertDialogBuilder(this@MainActivity)
                        .setView(View.inflate(this@MainActivity,R.layout.layout_edit_user,null).apply {
                            val listener = {
                                when {
                                    editName.text?.trim()?.isNotEmpty() != true || editName.text?.trim()?.length != 11 -> {
                                        editName.error = "账号格式有误"
                                        editName.requestFocus()
                                        false
                                    }
                                    editPass.text?.trim()?.isNotEmpty() != true || editPass.text?.trim()?.length?:0 < 6 -> {
                                        editPass.error = "密码格式有误"
                                        editPass.requestFocus()
                                        false
                                    }
                                    else -> {
                                        val value = ContentValues()
                                        value.put(UserEntry.COULMN_NAME,editName.text?.trim()?.toString()?:"")
                                        value.put(UserEntry.COULMN_PASS,editPass.text?.trim()?.toString()?:"")
                                        value.put(UserEntry.COULMN_AGE,editAge.text?.trim()?.toString()?.toLong())
                                        value.put(UserEntry.COULMN_ID,it.id)
                                        update(value,it.id)
                                        dialog?.dismiss()
                                        true
                                    }
                                }


                            }
                            editName.text?.append(it.name)
                            editPass.text?.append(it.password)
                            editAge.text?.append(it.age.toString())
                            editAge.setOnEditorActionListener { v, actionId, event ->
                                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                                    return@setOnEditorActionListener listener.invoke()
                                }
                                false
                            }
                            editDone.setOnClickListener {
                                listener.invoke()
                            }
                        })
                        .setTitle("编辑账号")
                        .create()
                dialog?.show()
            },null)
            listMain.adapter = adapter
            adapter?.setOnSelectModeListener {
                selectMode = it
                if (it) {
                    supportActionBar?.title = "选择模式"
                    supportActionBar?.subtitle = "已选择 0 项"
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    addAccount.shrink(true)
                    addAccount.icon = getDrawable(R.drawable.ic_delete_black_24dp)
                }else {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.subtitle = null
                    supportActionBar?.title = this@MainActivity.getString(R.string.app_name)
                    addAccount.extend(true)
                    addAccount.icon = getDrawable(R.drawable.ic_person_add_black_24dp)
                    selectList.clear()
                }
                val colorInt = if (it) {
                    Color.parseColor("#FF1744")
                }else {
                    getColor(R.color.colorAccent)
                }
                addAccount.setTextColor(colorInt)
                addAccount.iconTint = ColorStateList.valueOf(colorInt)
                addAccount.rippleColor = ColorStateList.valueOf(colorInt)

            }
            adapter?.setOnItemSelectListener { item, position, isSelect ->
                if (isSelect)
                    selectList[item.id] = item.id
                else
                    selectList.remove(item.id)
                supportActionBar?.subtitle = "已选择 ${selectList.size} 项"
            }
        }
        cursor?.close()
    }

    override fun onBackPressed() {
        if (adapter?.selectMode == true)
            adapter?.selectMode = false
        else
            super.onBackPressed()
    }
}
