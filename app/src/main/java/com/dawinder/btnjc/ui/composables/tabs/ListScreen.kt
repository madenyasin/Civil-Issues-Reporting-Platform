package com.dawinder.btnjc.ui.composables.tabs

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dawinder.btnjc.ui.theme.md_theme_light_inversePrimary
import com.dawinder.btnjc.ui.theme.md_theme_light_onSurface
import com.dawinder.btnjc.ui.theme.md_theme_light_primary
import com.dawinder.btnjc.ui.theme.md_theme_light_secondary
import com.dawinder.btnjc.ui.theme.typography
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

/**
 * Composable function that represents the list screen of the application.
 */
@Composable
fun ListScreen() {
    // Reference to the Firebase database
    val database = Firebase.database
    val postsRef = database.getReference("posts")

    // State to hold the list of posts
    val postsState = remember { mutableStateOf<List<Post>>(emptyList()) }

    // Function to fetch posts from Firebase
    val fetchPosts: () -> Unit = {
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let { posts.add(it) }
                }
                postsState.value = posts
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching posts", error.toException())
            }
        })
    }

    // Fetch posts when the composable is first launched
    LaunchedEffect(Unit) {
        fetchPosts()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (postsState.value.isEmpty()) {
            Text(
                text = "no_posts_available",
                style = typography.titleLarge,
                color = md_theme_light_inversePrimary
            )
        } else {
            LazyColumn {
                items(postsState.value) { post ->
                    PostCard(post)
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = typography.titleMedium,
                color = md_theme_light_primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.description,
                style = typography.bodyLarge,
                color = md_theme_light_onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Posted by: ${post.userName}",
                style = typography.bodySmall,
                color = md_theme_light_secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            post.latitude?.let { lat ->
                post.longitude?.let { long ->
                    Text(
                        text = "Location: $lat, $long",
                        style = typography.bodySmall,
                        color = md_theme_light_secondary
                    )
                }
            }
        }
    }
}

