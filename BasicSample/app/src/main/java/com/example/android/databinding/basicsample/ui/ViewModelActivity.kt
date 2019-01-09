/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.databinding.basicsample.ui

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.ColorStateList
import android.databinding.Observable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.android.databinding.basicsample.R
import com.example.android.databinding.basicsample.R.id.progressBar
import com.example.android.databinding.basicsample.data.Popularity
import com.example.android.databinding.basicsample.data.ProfileObservableViewModel
import se.snylt.witch.android.Witch
import se.snylt.witch.annotations.Bind
import se.snylt.witch.annotations.BindData
import se.snylt.witch.annotations.Data
import se.snylt.witch.annotations.Setup

class ViewModelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewmodel_profile)

        val viewModel = ViewModelProviders.of(this).get(ProfileObservableViewModel::class.java)
        Binder(viewModel, this)
    }

    // Binds view model state to views. Unlike the default binding system, there are no bindings declared
    // in the layout xml and there are no binding adapters. All bindings happen in this class.
    //
    // The view model is used for holding state in this case but could be replaced
    // with any state container.
    //
    // Key concepts:
    // - Less ceremony (No xml declarations. No adapters. No ViewModel dependency)
    // - Centralization (All bindings in one class)
    // - Simplicity (Five different annotations)
    //
    // https://github.com/sedstrom/Witch-Android
    class Binder (private val viewModel: ProfileObservableViewModel, private val view: Activity) {

        init {
            // Observe any change to view model.
            // Changes to individual properties are detected automatically.
            viewModel.changed.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    Witch.bind(this@Binder, view)
                }
            })
            Witch.bind(this@Binder, view)
        }

        @Setup(id = R.id.like_button)
        fun onLike(button: View) { button.setOnClickListener { viewModel.onLike() } }

        @Data
        fun popularity(): Popularity { return viewModel.getPopularity() }

        @BindData(id = R.id.name, view = TextView::class, set = "text")
        fun name(): String { return viewModel.name }

        @BindData(id = R.id.lastname, view = TextView::class, set = "text")
        fun lastName(): String { return viewModel.lastName }

        @Bind(id = R.id.imageView)
        fun bindPopularityImage(image: ImageView, popularity: Popularity) {
            image.setImageDrawable(
                    when (popularity) {
                        Popularity.NORMAL -> {
                            ContextCompat.getDrawable(image.context, R.drawable.ic_person_black_96dp)
                        }
                        Popularity.POPULAR -> {
                            ContextCompat.getDrawable(image.context, R.drawable.ic_whatshot_black_96dp)
                        }
                        Popularity.STAR -> {
                            ContextCompat.getDrawable(image.context, R.drawable.ic_whatshot_black_96dp)
                        }
                }
            )
            val color = ColorStateList.valueOf(getAssociatedColor(popularity, image.context))
            ImageViewCompat.setImageTintList(image, color)
        }

        @BindData(id = R.id.likes, view = TextView::class, set = "text")
        fun likes(): String { return "${viewModel.likes}" }

        @BindData(id = R.id.progressBar, view = View::class, set = "visibility")
        fun progressVisibility(): Int {
            return if (viewModel.likes > 0) { View.VISIBLE } else { View.GONE }
        }

        @Bind(id = R.id.progressBar)
        fun progressTint(view: ProgressBar, popularity: Popularity) {
            val color = getAssociatedColor(popularity, view.context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.progressTintList = ColorStateList.valueOf(color)
            }
        }

        @BindData(id = progressBar, view = ProgressBar::class, set = "progress")
        fun progress(): Int{
           return (viewModel.likes * viewModel.max / 5).coerceAtMost(viewModel.max)
        }

        private fun getAssociatedColor(popularity: Popularity, context: Context): Int {
            return when (popularity) {
                Popularity.NORMAL -> context.theme.obtainStyledAttributes(
                        intArrayOf(android.R.attr.colorForeground)).getColor(0, 0x000000)
                Popularity.POPULAR -> ContextCompat.getColor(context, R.color.popular)
                Popularity.STAR -> ContextCompat.getColor(context, R.color.star)
            }
        }
    }
}
