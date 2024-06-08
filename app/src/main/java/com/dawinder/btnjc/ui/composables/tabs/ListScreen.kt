package com.dawinder.btnjc.ui.composables.tabs

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dawinder.btnjc.ui.theme.md_theme_light_inversePrimary
import com.dawinder.btnjc.ui.theme.md_theme_light_onSurface
import com.dawinder.btnjc.ui.theme.md_theme_light_primary
import com.dawinder.btnjc.ui.theme.md_theme_light_secondary
import com.dawinder.btnjc.ui.theme.typography
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase



@Composable
fun ListScreen() {
    val database = Firebase.database
    val postsRef = database.getReference("posts")
    val postsState = remember { mutableStateOf<List<Post>>(emptyList()) }

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

    LaunchedEffect(Unit) {
        fetchPosts()
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 75.dp), contentAlignment = Alignment.Center) {
        if (postsState.value.isEmpty()) {
            Text(
                text = "No posts available",
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.userProfilePictureUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Posted by: ${post.userName}",
                    style = typography.bodySmall,
                    color = md_theme_light_secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
            post.latitude?.let { lat ->
                post.longitude?.let { long ->
                    Text(
                        text = "Location: $lat, $long",
                        style = typography.bodySmall,
                        color = md_theme_light_secondary
                    )
                }
            }
            post.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(model = url),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}
