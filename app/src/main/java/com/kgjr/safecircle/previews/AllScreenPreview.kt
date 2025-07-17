package com.kgjr.safecircle.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kgjr.safecircle.ui.layouts.AddPlaceScreen
import com.kgjr.safecircle.ui.layouts.CustomTopBar
import com.kgjr.safecircle.ui.layouts.InviteCodeScreen
import com.kgjr.safecircle.ui.layouts.JoinCircleScreen
import com.kgjr.safecircle.ui.layouts.LogoutConfirmationDialog
import com.kgjr.safecircle.ui.layouts.PlaceCheckInScreen



@Preview(showBackground = true)
@Composable
fun JoinCircleScreenPreview() {
    JoinCircleScreen(onBackPress = {

    }, onSubmitPress = {

    })
}

@Preview(showBackground = true)
@Composable
fun PlaceCheckInScreenPreview() {
    PlaceCheckInScreen(
        onBackPress = {

        }
    )
}

@Preview(showBackground = true)
@Composable
fun InviteCodeScreenPreview() {
    // You might want to wrap in your app theme if you have one
    Surface {
        InviteCodeScreen(code = "ABC123")
    }
}

@Preview
@Composable
fun AddPlaceScreenPreview(){
    AddPlaceScreen(placeType = "Home", radius = 300, "", onSavePlace = { data, bool, placeId ->

    }, onBackPress = {

    })
}
@Composable
@Preview(showBackground = true)
fun LogoutConfirmationDialogPreview() {
    LogoutConfirmationDialog(
        onDismiss = {},
        onConfirmLogout = {}
    )
}

@Preview
@Composable
fun CustomTopBarPreview(){
    CustomTopBar(
        profileUrl = "https://picsum.photos/536/354"
    ){

    }
}