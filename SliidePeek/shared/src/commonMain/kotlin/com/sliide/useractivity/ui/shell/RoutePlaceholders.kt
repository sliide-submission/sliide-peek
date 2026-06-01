package com.sliide.useractivity.ui.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sliide.useractivity.ui.components.ContentState
import com.sliide.useractivity.ui.components.ContentStateContainer
import com.sliide.useractivity.ui.components.InitialsAvatar
import com.sliide.useractivity.ui.components.SectionHeader
import com.sliide.useractivity.ui.components.SegmentedFilter
import com.sliide.useractivity.ui.components.StatusChip
import com.sliide.useractivity.ui.components.UserFilter
import com.sliide.useractivity.ui.navigation.AppRoute
import com.sliide.useractivity.ui.theme.selectionContainerColor

private data class DemoUser(
    val id: Long,
    val initials: String,
    val name: String,
    val email: String,
    val gender: String,
    val active: Boolean,
)

private data class DemoPost(val id: Long, val title: String)
private data class DemoTodo(val title: String, val due: String, val done: Boolean)

private val demoUsers = listOf(
    DemoUser(1, "AS", "Aarav Sharma", "aarav.sharma@example.com", "Male", true),
    DemoUser(2, "PN", "Priya Nair", "priya.nair@example.com", "Female", true),
    DemoUser(3, "MB", "Marcus Bauch", "marcus.bauch@example.com", "Male", false),
    DemoUser(4, "EV", "Elena Voss", "elena.voss@example.com", "Female", true),
    DemoUser(5, "TL", "Tobias Lang", "tobias.lang@example.com", "Male", false),
    DemoUser(6, "YT", "Yuki Tanaka", "yuki.tanaka@example.com", "Female", true),
)

private val demoPosts = listOf(
    DemoPost(101, "Scaling the payments ledger"),
    DemoPost(102, "Notes on KYC onboarding drop-off"),
    DemoPost(103, "Refactoring the fraud rules engine"),
)

private val demoTodos = listOf(
    DemoTodo("Review AML thresholds", "Due Jun 12", true),
    DemoTodo("Ship card dispute flow v2", "Due Jun 18", false),
    DemoTodo("Audit logging coverage", "Due Jun 20", false),
)

@Composable
internal fun UserListPlaceholder(
    selectedUserId: Long?,
    onUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    compactRows: Boolean = false,
) {
    var filter by remember { mutableStateOf(UserFilter.All) }
    Column(
        modifier = modifier.padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SegmentedFilter(selected = filter, onSelected = { filter = it })
        demoUsers
            .filter { user ->
                when (filter) {
                    UserFilter.All -> true
                    UserFilter.Active -> user.active
                    UserFilter.Inactive -> !user.active
                }
            }
            .forEach { user ->
                UserRowPlaceholder(
                    user = user,
                    selected = user.id == selectedUserId,
                    compact = compactRows,
                    onClick = { onUserClick(user.id) },
                )
            }
    }
}

@Composable
internal fun UserDetailPlaceholder(
    userId: Long,
    onPostsClick: (Long) -> Unit,
    onTodosClick: (Long) -> Unit,
    onPostClick: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = demoUsers.firstOrNull { it.id == userId } ?: demoUsers.first()
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileHeader(user)
        BoxWithConstraints {
            if (maxWidth >= 520.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    PostsSection(user.id, onPostsClick, onPostClick, Modifier.weight(1f))
                    TodosSection(user.id, onTodosClick, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PostsSection(user.id, onPostsClick, onPostClick)
                    TodosSection(user.id, onTodosClick)
                }
            }
        }
    }
}

@Composable
internal fun PostsListPlaceholder(
    userId: Long,
    onPostClick: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(title = "Posts", count = 8)
        demoPosts.forEach { post -> PostCard(post = post, onClick = { onPostClick(userId, post.id) }) }
        ContentStateContainer(
            state = ContentState.Loading,
            loadingRows = 2,
        ) {}
    }
}

@Composable
internal fun TodosListPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(title = "Todos", count = 5)
        demoTodos.forEach { todo -> TodoRow(todo) }
        ContentStateContainer(
            state = ContentState.Empty,
            emptyTitle = "No pending todos",
            emptyBody = "Completed and pending states reuse the same shell components.",
            emptyActionLabel = "Show all todos",
            onEmptyAction = {},
        ) {}
    }
}

@Composable
internal fun PostDetailPlaceholder(
    postId: Long,
    modifier: Modifier = Modifier,
) {
    val post = demoPosts.firstOrNull { it.id == postId } ?: demoPosts.first()
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(appBodyPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = post.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar("PN", size = 28.dp)
            Text("Priya Nair", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        PlaceholderParagraph(lines = 4)
        SectionHeader(title = "Comments", count = 3)
        CommentPlaceholder("MR", "Maya Reed", "maya.reed@example.com")
        CommentPlaceholder("DP", "Devon Pierce", "devon.pierce@example.com")
        ContentStateContainer(
            state = ContentState.Error("Comments can retry independently from the post body."),
            onRetry = {},
            loadingRows = 2,
        ) {}
    }
}

@Composable
internal fun NoSelectionPlaceholder(modifier: Modifier = Modifier) {
    ContentStateContainer(
        modifier = modifier,
        state = ContentState.Empty,
        emptyTitle = "Select a user",
        emptyBody = "Choose a user from the list to preview their activity here.",
    ) {}
}

@Composable
private fun UserRowPlaceholder(
    user: DemoUser,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectionContainerColor() else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Row(
                modifier = Modifier.padding(11.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InitialsAvatar(user.initials, size = if (compact) 28.dp else 38.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = if (compact) if (user.active) "active" else "inactive" else user.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (!compact) {
                    StatusChip(label = if (user.active) "Active" else "Inactive", active = user.active)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: DemoUser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialsAvatar(user.initials, size = 52.dp)
        Column(Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                StatusChip(label = if (user.active) "Active" else "Inactive", active = user.active)
                StatusChip(label = user.gender)
            }
        }
    }
}

@Composable
private fun PostsSection(
    userId: Long,
    onPostsClick: (Long) -> Unit,
    onPostClick: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionHeader(title = "Posts", count = 8, onClick = { onPostsClick(userId) })
        demoPosts.take(2).forEach { post -> PostCard(post = post, onClick = { onPostClick(userId, post.id) }) }
    }
}

@Composable
private fun TodosSection(
    userId: Long,
    onTodosClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionHeader(title = "Todos", count = 5, onClick = { onTodosClick(userId) })
        demoTodos.take(2).forEach { todo -> TodoRow(todo) }
    }
}

@Composable
private fun PostCard(post: DemoPost, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(post.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            PlaceholderParagraph(lines = 2)
        }
    }
}

@Composable
private fun TodoRow(todo: DemoTodo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.width(18.dp).height(18.dp),
                shape = RoundedCornerShape(5.dp),
                color = if (todo.done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                if (todo.done) Box(contentAlignment = Alignment.Center) { Text("✓", color = MaterialTheme.colorScheme.surface, style = MaterialTheme.typography.labelSmall) }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    color = if (todo.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(todo.due, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            StatusChip(label = if (todo.done) "Done" else "Pending", active = todo.done)
        }
    }
}

@Composable
private fun CommentPlaceholder(initials: String, name: String, email: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(11.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            InitialsAvatar(initials, size = 28.dp)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                PlaceholderParagraph(lines = 2)
            }
        }
    }
}

@Composable
private fun PlaceholderParagraph(lines: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(lines) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == lines - 1) 0.68f else 1f)
                    .height(9.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp)),
            )
        }
    }
}
