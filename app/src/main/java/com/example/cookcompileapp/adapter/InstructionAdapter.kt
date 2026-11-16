package com.example.cookcompileapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cookcompileapp.databinding.ItemInstructionStepBinding
import com.example.cookcompileapp.model.InstructionStep

class InstructionAdapter(
    private val instructions: List<InstructionStep>
) : RecyclerView.Adapter<InstructionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstructionStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(instructions[position])
    }

    override fun getItemCount(): Int = instructions.size

    inner class ViewHolder(private val binding: ItemInstructionStepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(step: InstructionStep) {
            binding.tvStepNumber.text = step.stepNumber.toString()
            binding.tvStepDescription.text = step.description
        }
    }
}
