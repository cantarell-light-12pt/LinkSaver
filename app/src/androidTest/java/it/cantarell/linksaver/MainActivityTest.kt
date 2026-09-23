package it.cantarell.linksaver

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addLinkScreen_isShownOnLaunch() {
        composeRule.onNodeWithText("Add link").assertExists()
    }

    @Test
    fun invalidUrl_showsErrorFromViewModel() {
        composeRule.onNode(hasSetTextAction() and hasText("URL")).performTextInput("not a url")
        composeRule.onNodeWithText("Save").performClick()

        composeRule.onNodeWithText("Enter a valid http or https URL").assertExists()
    }
}
