/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.dagger.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android.dagger.MyApplication
import com.example.android.dagger.R
import com.example.android.dagger.main.MainActivity
import com.example.android.dagger.registration.enterdetails.EnterDetailsFragment
import com.example.android.dagger.registration.termsandconditions.TermsAndConditionsFragment
import javax.inject.Inject

class RegistrationActivity : AppCompatActivity() {

    // Stores an instance of RegistrationComponent so that its Fragments can access it
    lateinit var registrationComponent: RegistrationComponent

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var registrationViewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        /**
         * 这个就比较好理解了：
         *  1. 按照之前有些blog的说法被自定义的注解修饰的都是单利的
         *    比如我们的 RegistrationViewModel ，在 下面的         registrationComponent.inject(this) 追溯中
         *    确实是使用到了 registrationViewModelProvider ，并且是单利的
         *
         *  2. 但是这个单利的前提是
         *  (application as MyApplication).appComponent
         *             .registrationComponent().create() 作用域内
         *
         *         this.registrationViewModelProvider = DoubleCheck.provider(RegistrationViewModel_Factory.create(appComponent.userManagerProvider));
         *         这个里面的对象是每次创建的
         *
         *  3.         Log.i(
         *             "RegistrationActivity",
         *             "---------------验证单利的范围-----------" + registrationViewModel
         *         );
         *  打印日志验证
         *
         *  只需要注册，登录，退出登录，解绑重新注册 就会导致界面重新创建
         *
         */
        // Creates an instance of Registration component by grabbing the factory from the app graph
        registrationComponent = (application as MyApplication).appComponent
            .registrationComponent().create()

        // Injects this activity to the just created Registration component
        registrationComponent.inject(this)

        /**
         *  ---------------验证单利的范围-----------com.example.android.dagger.registration.RegistrationViewModel@e768dcb
         *  ---------------验证单利的范围-----------com.example.android.dagger.registration.RegistrationViewModel@d62d657
         *
         */
        Log.i(
            "RegistrationActivity",
            "---------------验证单利的范围-----------" + registrationViewModel
        );
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_holder, EnterDetailsFragment())
            .commit()
    }

    /**
     * Callback from EnterDetailsFragment when username and password has been entered
     */
    fun onDetailsEntered() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_holder, TermsAndConditionsFragment())
            .addToBackStack(TermsAndConditionsFragment::class.java.simpleName)
            .commit()
    }

    /**
     * Callback from T&CsFragment when TCs have been accepted
     */
    fun onTermsAndConditionsAccepted() {
        registrationViewModel.registerUser()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
