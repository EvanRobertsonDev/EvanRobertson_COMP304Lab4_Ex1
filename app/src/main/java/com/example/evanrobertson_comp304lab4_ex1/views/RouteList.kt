package com.example.evanrobertson_comp304lab4_ex1.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.evanrobertson_comp304lab4_ex1.data.Route
import com.example.evanrobertson_comp304lab4_ex1.viewModels.RoutesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RouteList(
    onRouteClicked: (Route) -> Unit,
    routes: List<Route>,
    viewModel: RoutesViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(routes) { route ->
            RouteListItem(
                route = route,
                onRouteClicked = onRouteClicked,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteListItem(route: Route, onRouteClicked: (Route) -> Unit, viewModel: RoutesViewModel) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clickable { onRouteClicked(route) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {

            //Display Route Name
            Text(
                text = route.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(6.dp)
            )

            //Display Start Point
            Text(
                text = "Start: ${kotlin.math.round(route.startPoint.latitude * 1000) / 1000}, ${kotlin.math.round(route.startPoint.longitude * 1000) / 1000}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(6.dp)
            )

            //Display End Point
            Text(
                text = "End: ${kotlin.math.round(route.endPoint.latitude * 1000) / 1000}, ${kotlin.math.round(route.endPoint.longitude * 1000) / 1000}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(6.dp)
            )

            //Display Additional Information in Chips
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
            ) {
                //Stops
                SuggestionChip(
                    onClick = { },
                    label = { Text(text = "Stops: ${route.waypoints.size - 2}") }
                )
            }
            //Delete button
            Button(
                onClick = {
                    //Remove route from repo using view model
                    viewModel.removeRoute(
                        id = route.id,
                        name = route.name,
                        startPoint = route.startPoint,
                        endPoint = route.endPoint,
                        waypoints = route.waypoints
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //Delete Icon
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete Route"
                )
            }
        }
    }
}