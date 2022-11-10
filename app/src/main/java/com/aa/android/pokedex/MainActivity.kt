package com.aa.android.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.aa.android.pokedex.api.entity.PokemonDTO
import com.aa.android.pokedex.model.UiState
import com.aa.android.pokedex.ui.theme.PokedexTheme
import com.aa.android.pokedex.viewmodel.MainViewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import dagger.hilt.android.AndroidEntryPoint
import java.nio.file.WatchEvent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                Screen()
            }
        }
    }
}

@Composable
fun Screen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    Scaffold(topBar = {
        TopAppBar(backgroundColor = MaterialTheme.colors.primary, title = {
            Image(painter = painterResource(id = R.drawable.pokemon_logo), null)
        })
    }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colors.background
        ) {
            NavHost(navController = navController, startDestination = "list") {
                composable(route = "list") {
                    PokemonList(pokemon = viewModel.pokemonLiveData) { pokemonName ->
                        navController.navigate("details/$pokemonName")
                    }
                }
                composable(route = "details/{pokemonName}",
                    arguments = listOf(
                        navArgument("pokemonName") {
                            type = NavType.StringType
                        }
                    )) {
                    PokemonDetails(viewModel = viewModel,
                    it.arguments?.getString("pokemonName") ?: "Pikachu")
                }
            }
        }
    }
}

@Composable
fun PokemonDetails(viewModel: MainViewModel, pokemonName: String) {
    viewModel.getPokemonDetail(pokemonName)
    val pokemonData = viewModel.pokemonDetails.observeAsState().value

    pokemonData?.let { data ->
        Column(modifier = Modifier.padding(4.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data.sprites.defaultFront)
                    .scale(Scale.FILL)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.pokemon_logo),
                contentDescription = stringResource(R.string.pokemon_description),
                modifier = Modifier.clip(CircleShape)
            )

            Spacer(modifier = Modifier.padding(20.dp))

            Text(text = "Name: " + data.name)
            Text(text = "Height: " + data.height.toString())
            Text(text = "Weight: " + data.weight.toString())
            Text(text = "Types: " + data.types.map { it.type.name }.toString())
            Text(
                text = stringResource(
                    R.string.pokemon_stats,
                    data.stats.firstOrNull()?.baseStat ?: "N/A",
                    data.stats.firstOrNull()?.effort ?: "N/A",
                    data.stats.firstOrNull()?.stat?.name ?: "N/A",
                )
            )
        }
    }
}

@Composable
fun PokemonList(pokemon: LiveData<UiState<List<String>>>, navigateDetail: (String) -> Unit) {
    val uiState: UiState<List<String>>? by pokemon.observeAsState()
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        uiState?.let {
            when (it) {
                is UiState.Loading -> {
                    items(20) {
                        PokemonItem(pokemon = "", isLoading = true) {}
                    }
                }
                is UiState.Ready -> {
                    items(it.data) { pkmn ->
                        PokemonItem(pokemon = pkmn, isLoading = false) { pokemonName ->
                            navigateDetail(pokemonName)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            textAlign = TextAlign.Center,
                            text = "Error loading list. Please try again later.",
                            color = MaterialTheme.colors.onBackground
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PokemonItem(pokemon: String, isLoading: Boolean, navigateDetail: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .placeholder(
                visible = isLoading,
                highlight = PlaceholderHighlight.shimmer(),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            navigateDetail(pokemon)
        }) {
        Text(
            text = pokemon.capitalize(Locale.current),
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PokedexTheme {
       // PokemonDetails(viewModel = viewModel(), pokemonName = "Pikachu")
    }
}