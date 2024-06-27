package com.example.foodmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private lateinit var lvfoodlist: ListView
private lateinit var Btadd: Button
private lateinit var BtVorrat: Button
private lateinit var BtRezepte: Button
private lateinit var itemAdapter: ArrayAdapter<String>
private lateinit var db: AppDatabase
private lateinit var foodItemDao: FoodItemDao

class MainActivity : AppCompatActivity() {
    private val vorratsliste = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        foodItemDao = db.foodItemDao()

        lvfoodlist = findViewById(R.id.lvfoodlist)
        Btadd = findViewById(R.id.btadd)
        BtVorrat = findViewById(R.id.btVorrat)
        BtRezepte = findViewById(R.id.btRezepte)

        itemAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, vorratsliste)
        lvfoodlist.adapter = itemAdapter

        loadFoodItems()

        lvfoodlist.setOnItemLongClickListener { _, _, pos, _ ->
            val itemName = vorratsliste[pos]
            showDeleteConfirmationDialog(itemName)
            true
        }

        Btadd.setOnClickListener {
            showAddFoodItemDialog()
        }

        BtRezepte.setOnClickListener {
            startRezepteActivity()
        }

        lvfoodlist.setOnItemClickListener { _, _, pos, _ ->
            val selectedFoodItem = vorratsliste[pos]
            showEditFoodItemDialog(selectedFoodItem)
        }
    }

    private fun loadFoodItems() {
        foodItemDao.getAll().observe(this, Observer { items ->
            items?.let {
                vorratsliste.clear()
                for (foodItem in it) {
                    val formattedItem = "${foodItem.name} - ${foodItem.quantity} Stück - ${foodItem.amountInGrams}g"
                    vorratsliste.add(formattedItem)
                }
                itemAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun addFoodItem(name: String, quantity: Int, amountInGrams: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val foodItem = FoodItem(name = name, quantity = quantity, amountInGrams = amountInGrams)
            foodItemDao.insert(foodItem)
            withContext(Dispatchers.Main) {
                loadFoodItems()
            }
        }
    }

    private fun deleteFoodItem(name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val foodItem = foodItemDao.findByName(name)
            foodItem?.let {
                foodItemDao.delete(it)
                withContext(Dispatchers.Main) {
                    loadFoodItems()
                }
            }
        }
    }

    private fun updateFoodItem(name: String, quantity: Int, amountInGrams: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val foodItem = foodItemDao.findByName(name)
            foodItem?.let {
                val updatedFoodItem = it.copy(quantity = quantity, amountInGrams = amountInGrams)
                foodItemDao.update(updatedFoodItem)
                withContext(Dispatchers.Main) {
                    loadFoodItems()
                }
            }
        }
    }

    private fun showAddFoodItemDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hinzufügen")

        val nameInput = EditText(this)
        nameInput.hint = "Name eingeben"
        nameInput.inputType = InputType.TYPE_CLASS_TEXT

        val quantityInput = EditText(this)
        quantityInput.hint = "Anzahl eingeben"
        quantityInput.inputType = InputType.TYPE_CLASS_NUMBER

        val amountInput = EditText(this)
        amountInput.hint = "Menge in Gramm eingeben"
        amountInput.inputType = InputType.TYPE_CLASS_NUMBER

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(nameInput)
        layout.addView(quantityInput)
        layout.addView(amountInput)

        builder.setView(layout)

        builder.setPositiveButton("OK") { _, _ ->
            val name = nameInput.text.toString()
            val quantity = quantityInput.text.toString().toIntOrNull() ?: 0
            val amountInGrams = amountInput.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty()) {
                Toast.makeText(applicationContext, "Kein Name eingegeben", Toast.LENGTH_SHORT).show()
            } else {
                addFoodItem(name, quantity, amountInGrams)
            }
        }

        builder.setNegativeButton("Abbrechen") { _, _ ->
            Toast.makeText(applicationContext, "Abgebrochen", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun showEditFoodItemDialog(selectedFoodItem: String) {
        val parts = selectedFoodItem.split(" - ", " Stück - ", "g")
        if (parts.size < 3) return

        val foodItemName = parts[0]
        val foodItemQuantity = parts[1].toIntOrNull() ?: 0
        val foodItemAmountInGrams = parts[2].toIntOrNull() ?: 0

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Bearbeiten: $foodItemName")

        val quantityInput = EditText(this)
        quantityInput.hint = "Anzahl eingeben"
        quantityInput.inputType = InputType.TYPE_CLASS_NUMBER
        quantityInput.setText(foodItemQuantity.toString())

        val amountInput = EditText(this)
        amountInput.hint = "Menge in Gramm eingeben"
        amountInput.inputType = InputType.TYPE_CLASS_NUMBER
        amountInput.setText(foodItemAmountInGrams.toString())

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(quantityInput)
        layout.addView(amountInput)

        builder.setView(layout)

        builder.setPositiveButton("OK") { _, _ ->
            val newQuantity = quantityInput.text.toString().toIntOrNull() ?: 0
            val newAmountInGrams = amountInput.text.toString().toIntOrNull() ?: 0

            updateFoodItem(foodItemName, newQuantity, newAmountInGrams)
        }

        builder.setNegativeButton("Abbrechen") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun startRezepteActivity() {
        val intent = Intent(this, RezepteActivity::class.java)
        startActivity(intent)
    }

    private fun enableEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showDeleteConfirmationDialog(itemName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Möchtest du dieses Objekt wirklich löschen?")
        builder.setPositiveButton("Ja") { _, _ ->
            deleteFoodItem(itemName)
        }
        builder.setNegativeButton("Abbrechen") { _, _ ->
            Toast.makeText(applicationContext, "Abgebrochen", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }
}
