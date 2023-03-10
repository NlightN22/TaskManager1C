package space.active.taskmanager1c.presentation.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.navOptions
import space.active.taskmanager1c.R

fun NavController.navigateWithAnim(@IdRes resId: Int, args: Bundle?) {
    this.navigate(resId, args,
        navOptions {
        anim {
            enter = R.anim.enter
            exit = R.anim.exit
            popEnter = R.anim.pop_enter
            popExit = R.anim.pop_exit
        }
    })
}

fun NavController.navigateWithAnim(navDirections: NavDirections) {
    this.navigate(navDirections,
        navOptions {
            anim {
                enter = R.anim.enter
                exit = R.anim.exit
                popEnter = R.anim.pop_enter
                popExit = R.anim.pop_exit
            }
        })
}