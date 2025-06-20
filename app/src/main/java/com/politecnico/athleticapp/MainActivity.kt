package com.politecnico.athleticapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.politecnico.athleticapp.databinding.ActivityMainBinding
import com.politecnico.athleticapp.ui.menu.NavItem
import com.politecnico.athleticapp.ui.menu.NavigationRVAdapter
import androidx.navigation.NavOptions

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navViewAdapter: NavigationRVAdapter
    private lateinit var navController: NavController // Hacerlo miembro de la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modern way to handle edge-to-edge and status bar appearance
        WindowCompat.setDecorFitsSystemWindows(window, false) // Enable edge-to-edge

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set status bar color and icons using modern API
        window.statusBarColor = Color.TRANSPARENT // Or Color.WHITE if you don't want full transparency initially
        val insetsController = WindowInsetsControllerCompat(window, binding.root)
        insetsController.isAppearanceLightStatusBars = true // True for dark icons on light background

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main) // Inicializar aquí

        // Listener para el icono de cerrar en el nav_header
        val headerView = navView.getHeaderView(0)
        val closeButton = headerView.findViewById<ImageView>(R.id.nav_close_icon)
        closeButton?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home_main, R.id.nav_workouts, R.id.nav_meal_plans, R.id.nav_progress_tracking, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // navView.setupWithNavController(navController) // Ya no usamos esto directamente para el menú

        setupRecyclerView(navView, drawerLayout)

        // Observar cambios de destino para actualizar el ítem seleccionado en el RecyclerView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            navViewAdapter.setSelectedItem(destination.id)
        }
    }

    private fun setupRecyclerView(navView: NavigationView, drawerLayout: DrawerLayout) {
        // Obtener la cabecera primero
        val headerView = navView.getHeaderView(0)
        // Luego encontrar el RecyclerView dentro de la cabecera
        val recyclerView = headerView.findViewById<RecyclerView>(R.id.nav_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val navItems = listOf(
            NavItem(R.id.nav_home_main, R.string.menu_home, R.drawable.ic_home_material, true), // Marcar home como seleccionado inicialmente
            NavItem(R.id.nav_workouts, R.string.menu_workouts, R.drawable.ic_workouts_placeholder), // Reemplaza R.string y R.drawable
            NavItem(R.id.nav_meal_plans, R.string.menu_meal_plans, R.drawable.ic_meal_plans_placeholder),
            NavItem(R.id.nav_progress_tracking, R.string.menu_progress_tracking, R.drawable.ic_progress_placeholder),
            NavItem(R.id.nav_settings, R.string.menu_settings, R.drawable.ic_settings_placeholder)
        )

        navViewAdapter = NavigationRVAdapter(navItems) { selectedNavItem ->
            if (navController.currentDestination?.id != selectedNavItem.id) {
                showLoading() // Asegurarse de llamar a showLoading de MainActivity
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.custom_fade_in)
                    .setExitAnim(R.anim.custom_fade_out)
                    .setPopEnterAnim(R.anim.custom_fade_in)
                    .setPopExitAnim(R.anim.custom_fade_out)
                    .build()
                navController.navigate(selectedNavItem.id, null, navOptions)
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        recyclerView.adapter = navViewAdapter

        // Establecer la selección inicial basada en el destino actual (si la app no empieza en home)
        navController.currentDestination?.id?.let {
            navViewAdapter.setSelectedItem(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> { // Este es el ID del ítem en res/menu/main.xml
                navController.navigate(R.id.nav_settings) // Este es el ID del destino en mobile_navigation.xml
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }
}