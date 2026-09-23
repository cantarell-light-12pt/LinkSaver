package it.cantarell.linksaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import it.cantarell.linksaver.ui.addlink.AddLinkRoute
import it.cantarell.linksaver.ui.theme.LinkSaverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinkSaverTheme {
                AddLinkRoute()
            }
        }
    }
}
