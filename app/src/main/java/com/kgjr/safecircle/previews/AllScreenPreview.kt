package com.kgjr.safecircle.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kgjr.safecircle.ui.layouts.AddPlaceScreen
import com.kgjr.safecircle.ui.layouts.JoinCircleScreen
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

@Preview
@Composable
fun AddPlaceScreenPreview(){
    AddPlaceScreen(placeType = "Home", radius = 300, "", onSavePlace = { data, bool, placeId ->

    }, onBackPress = {

    })
}