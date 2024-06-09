package com.dawinder.btnjc.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.dawinder.btnjc.R

sealed class NavItem {
    object Map :
        Item(path = NavPath.MAP.toString(), title = NavTitle.MAP, icon = Icons.Default.LocationOn)

    object List :
        Item(
            path = NavPath.LIST.toString(),
            title = NavTitle.LIST,
            icon = Icons.Default.List
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