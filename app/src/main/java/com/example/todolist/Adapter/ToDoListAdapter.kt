package com.example.todolist.Adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.IOnTaskTouchListener
import com.example.todolist.Model.ToDoItem
import com.example.todolist.R
import com.example.todolist.Utils.*
import com.example.todolist.databinding.TaskBinding

class UserDiffCallBack(
    private val oldList: List<ToDoItem>,
    private val newList: List<ToDoItem>
) : DiffUtil.Callback(){
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.getId() == newItem.getId()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }

}

class ToDoListAdapter(
    private val actionListener: IOnTaskTouchListener
) : RecyclerView.Adapter<ToDoListAdapter.ToDoItemViewHolder>(){

    var toDoList: List<ToDoItem> = emptyList()
    set(value) {
        val diffCallBack = UserDiffCallBack(field, value)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        field = value
        diffResult.dispatchUpdatesTo(this)
    }

    class ToDoItemViewHolder(
        val binding: TaskBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TaskBinding.inflate(inflater, parent, false)

        return ToDoItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoItemViewHolder, position: Int) {
        val toDoItem = toDoList[position]
        holder.binding.taskInfoTextView.text = toDoItem.getTaskText()
        holder.binding.taskInfoCheckBox.isChecked = toDoItem.isDone()
        setStrikeThruTextFlag(toDoItem.isDone(), holder.binding.taskInfoTextView)
        setImportanceIcon(toDoItem.getImportance(), holder.binding.importanceIcon)

        holder.binding.taskInfoContainer
            .setOnClickListener{
                actionListener.onChangeButtonClick(toDoItem.getId())
            }

        holder.binding.taskInfoContainer
            .setOnLongClickListener{
                showPopupMenuAction(it, position, toDoItem)
                return@setOnLongClickListener true

            }
        holder.binding.taskInfoCheckBox
            .setOnCheckedChangeListener {
                    _, isChecked ->
                run {
                    setStrikeThruTextFlag(isChecked, holder.binding.taskInfoTextView)
                    actionListener.onCheckboxClick(toDoItem, isChecked)
                }
            }
        holder.binding.infoTaskButton
            .setOnClickListener{
                showPopupMenuInfo(it, toDoItem)
            }
    }

    private fun setStrikeThruTextFlag(state: Boolean, textView: TextView) {
        if (state) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textView.setTextColor(ContextCompat.getColor(textView.context, R.color.light_tertiary))
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            textView.setTextColor(ContextCompat.getColor(textView.context, R.color.black))
        }
    }


    private fun showPopupMenuAction(view: View, position: Int, task: ToDoItem){
        val popupMenu = PopupMenu(view.context, view)
        val item1 = popupMenu.menu.add(Menu.NONE, MOVE_UP, Menu.NONE, view.context.getString(R.string.MoveUp))
            .apply { isEnabled = position > 0 }
        val item2 = popupMenu.menu.add(Menu.NONE, MOVE_DOWN, Menu.NONE, view.context.getString(R.string.MoveDown))
            .apply { isEnabled = position < toDoList.size - 1 }
        val item3 = popupMenu.menu.add(Menu.NONE, DELETE, Menu.NONE, view.context.getString(R.string.Delete))

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                 MOVE_UP -> {
                     actionListener.onTaskMove(task, -1)
                     true
                }
                MOVE_DOWN -> {
                    actionListener.onTaskMove(task, 1)
                    true
                }
                DELETE -> {
                    actionListener.onTaskDelete(task)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showPopupMenuInfo(view: View, task: ToDoItem){
        val popupMenu = PopupMenu(view.context, view)

        val dateOfCreateString = view.context.getString(R.string.date_of_create)
        val dateOfChangeString = view.context.getString(R.string.date_of_change)
        val dateOfCreateText = "$dateOfCreateString: ${getFormattedDate(task.getDateOfCreate())}"
        val dateOfChangeText = "$dateOfChangeString: ${getFormattedDate(task.getDateOfChange())}"

        val item1 = popupMenu.menu.add(Menu.NONE, DATE_OF_CREATE, Menu.NONE, dateOfCreateText)
        val item2 = popupMenu.menu.add(Menu.NONE, DATE_OF_CHANGE, Menu.NONE, dateOfChangeText)

        popupMenu.show()
    }

    override fun getItemCount(): Int = toDoList.size

    private fun setImportanceIcon(importance: ToDoItem.Importance, importanceIcon: ImageView){
        when(importance){
            ToDoItem.Importance.DEFAULT -> importanceIcon.visibility = View.GONE
            ToDoItem.Importance.LOW -> {
                importanceIcon.visibility = View.VISIBLE
                importanceIcon.setImageResource(R.drawable.low_important)
            }
            ToDoItem.Importance.HIGH -> {

                importanceIcon.visibility = View.VISIBLE
                importanceIcon.setImageResource(R.drawable.important)
            }
        }
    }

    companion object{
        const val MOVE_UP = 0
        const val MOVE_DOWN = 1
        const val DELETE = 2
        const val DATE_OF_CREATE = 3
        const val DATE_OF_CHANGE = 4
    }
}
