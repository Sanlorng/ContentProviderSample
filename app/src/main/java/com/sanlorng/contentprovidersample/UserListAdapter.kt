package com.sanlorng.contentprovidersample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_user_item.view.*

class UserListAdapter(private val list: ArrayList<UserEntry>,
                      private val itemListener: ((UserEntry ) -> Unit)? = null,
                      private val itemLongListener: ((UserEntry ) -> Unit)? = null): RecyclerView.Adapter<UserListAdapter.BaseViewHolder>() {
    private var itemPosition = -1
    var selectModeListener:((isSelectMode:Boolean) -> Unit)? = null
    var itemSelectListener:((item:UserEntry,position: Int,isSelect: Boolean) -> Unit)? = null
    var selectMode = false
    set(value) {
        if (value != field) {
            field = value
            if (field)
                repeat(list.size) {
                    selectItems.add(itemPosition == it)
                }
            else {
                itemPosition = -1
                selectItems.clear()
            }
            selectModeListener?.invoke(field)
            notifyDataSetChanged()
        }
    }
    var selectItems: ArrayList<Boolean> = ArrayList(0)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.itemView.run {
            itemSelect.isVisible = selectMode
            if (selectMode) {
                itemSelect.setOnCheckedChangeListener { _, isChecked ->
                    selectItems[position] = isChecked
                    itemSelectListener?.invoke(list[position],position,isChecked)
                }
                itemSelect.isChecked = selectItems[position]
            }
            list[position].apply {
                item_name.text = name
                item_pass.text = password
                setOnClickListener {
                    if (selectMode.not())
                        itemListener?.invoke(this)
                    else
                        itemSelect.isChecked = itemSelect.isChecked.not()
                }
                setOnLongClickListener {
                    if (itemLongListener!= null)
                        itemLongListener.invoke(this)
                    else {
                        itemPosition = position
                        selectMode = selectMode.not()
                    }
                    true
                }
            }
        }
    }

    fun setOnSelectModeListener(value: ((isSelectMode:Boolean) -> Unit)){
        selectModeListener = value
    }
    fun setOnItemSelectListener(value: ((item:UserEntry,position: Int,isSelect: Boolean) -> Unit)) {
        itemSelectListener = value
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_user_item,parent,false))
    }
    inner class BaseViewHolder(view: View): RecyclerView.ViewHolder(view)
}