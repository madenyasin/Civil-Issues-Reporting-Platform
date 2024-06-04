package com.dawinder.btnjc.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person

sealed class NavItem {
    object Map :
        Item(path = NavPath.MAP.toString(), title = NavTitle.MAP, icon = Icons.Default.Home)

    object List :
        Item(
            path = NavPath.LIST.toString(),
            title = NavTitle.LIST,
            icon = Icons.AutoMirrored.Filled.List
        )

    object Profile :
        Item(
            path = NavPath.PROFILE.toString(), title = NavTitle.PROFILE, icon = Icons.Default.Person
        )

    object SignIn :
        Item(
            path = NavPath.SIGN_IN.toString(),
            title = NavTitle.SIGN_IN,
            icon = Icons.Default.AccountBox
        )

}