package com.dawinder.btnjc.ui.composables.tabs

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp),
        contentAlignment = Alignment.Center
    ) {
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
            .shadow(4.dp, shape =  RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.userProfilePictureUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(2.dp, md_theme_light_primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = post.title,
                        style = typography.titleMedium,
                        color = md_theme_light_primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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

            post.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(model = url),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)
                ))
            }
        }
    }
}
