<template>
  <div class="admin-layout">
    <nav class="admin-nav">
      <p class="eyebrow" style="padding:16px 20px 8px;border-bottom:1px solid var(--border)">管理后台</p>
      <router-link to="/admin" class="admin-nav-link" :class="{active:$route.path==='/admin'}"><span class="admin-nav-num">01</span>仪表盘</router-link>
      <router-link to="/admin/users" class="admin-nav-link" :class="{active:$route.path.startsWith('/admin/users')}"><span class="admin-nav-num">02</span>用户管理</router-link>
      <router-link to="/admin/categories" class="admin-nav-link" :class="{active:$route.path.startsWith('/admin/categories')}"><span class="admin-nav-num">03</span>分类管理</router-link>
      <router-link to="/admin/tags" class="admin-nav-link" :class="{active:$route.path.startsWith('/admin/tags')}"><span class="admin-nav-num">04</span>标签管理</router-link>
      <router-link to="/admin/pages" class="admin-nav-link" :class="{active:$route.path.startsWith('/admin/pages')}"><span class="admin-nav-num">05</span>页面管理</router-link>
      <router-link to="/admin/logs" class="admin-nav-link" :class="{active:$route.path.startsWith('/admin/logs')}"><span class="admin-nav-num">06</span>操作日志</router-link>
    </nav>
    <div class="admin-content">
      <router-view v-slot="{ Component }">
        <Transition name="admin-child" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.admin-layout{display:flex;min-height:calc(100vh - 52px)}
.admin-nav{width:200px;flex-shrink:0;background:var(--card);border-right:1px solid var(--border);overflow-y:auto}
.admin-content{flex:1;padding:24px 32px;overflow-y:auto;overflow-x:auto}
.admin-nav-link{display:flex;align-items:center;gap:10px;padding:12px 20px;font-size:13px;text-transform:uppercase;letter-spacing:.06em;color:var(--muted);border-bottom:1px solid var(--border);transition:color .15s,background .15s}
.admin-nav-link:hover{color:var(--fg);background:color-mix(in srgb,var(--fg) 4%,transparent)}
.admin-nav-link.active{color:var(--fg);background:color-mix(in srgb,var(--fg) 8%,transparent)}
.admin-nav-num{font-family:var(--mono);font-size:11px;opacity:.5}
@media(max-width:768px){.admin-nav{display:none}.admin-content{padding:16px 12px}}
</style>
