<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <nav class="admin-nav">
      <p class="hw-eyebrow" style="padding:16px 20px 8px;border-bottom:1px solid var(--color-border)">管理后台</p>
      <router-link to="/admin" class="admin-nav-link" :class="{ active: $route.path === '/admin' }">
        <span class="admin-nav-num">01</span> 仪表盘
      </router-link>
      <router-link to="/admin/users" class="admin-nav-link" :class="{ active: $route.path.startsWith('/admin/users') }">
        <span class="admin-nav-num">02</span> 用户管理
      </router-link>
      <router-link to="/admin/categories" class="admin-nav-link" :class="{ active: $route.path.startsWith('/admin/categories') }">
        <span class="admin-nav-num">03</span> 分类管理
      </router-link>
      <router-link to="/admin/tags" class="admin-nav-link" :class="{ active: $route.path.startsWith('/admin/tags') }">
        <span class="admin-nav-num">04</span> 标签管理
      </router-link>
      <router-link to="/admin/pages" class="admin-nav-link" :class="{ active: $route.path.startsWith('/admin/pages') }">
        <span class="admin-nav-num">05</span> 页面管理
      </router-link>
      <router-link to="/admin/logs" class="admin-nav-link" :class="{ active: $route.path.startsWith('/admin/logs') }">
        <span class="admin-nav-num">06</span> 操作日志
      </router-link>
    </nav>

    <!-- Content -->
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.admin-layout { display: flex; min-height: 80vh; }
.admin-nav {
  width: 200px; flex-shrink: 0;
  background: var(--color-card); border-right: 1px solid var(--color-border);
}
.admin-nav-link {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px; font-size: 0.85rem; text-transform: uppercase;
  letter-spacing: 0.06em; color: var(--color-muted);
  border-bottom: 1px solid var(--color-border);
  transition: color 150ms, background 150ms;
}
.admin-nav-link:hover { color: var(--color-foreground); background: rgba(255,255,255,0.04); }
.admin-nav-link.active { color: var(--color-foreground); background: rgba(255,255,255,0.08); }
.admin-nav-num { font-family: var(--font-mono); font-size: 0.7rem; opacity: 0.5; }
.admin-content { flex: 1; padding: 24px 32px; overflow-x: auto; }

@media (max-width: 768px) {
  .admin-nav { display: none; }
  .admin-content { padding: 16px; }
}
</style>
