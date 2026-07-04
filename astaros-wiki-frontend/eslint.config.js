import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import prettier from 'eslint-config-prettier'

export default tseslint.config(
  // —— 全局忽略 ——
  { ignores: ['dist/**', 'node_modules/**', '.vite/**'] },

  // —— JS / TS 基础规则 ——
  js.configs.recommended,
  ...tseslint.configs.recommended,

  // —— Vue 文件 ——
  ...pluginVue.configs['flat/essential'],
  {
    files: ['*.vue', '**/*.vue'],
    languageOptions: { parser: vueParser, parserOptions: { parser: tseslint.parser } },
  },

  // —— Prettier（关闭冲突规则） ——
  prettier,

  // —— 项目定制 ——
  {
    languageOptions: {
      globals: {
        console: 'readonly',
        localStorage: 'readonly',
        document: 'readonly',
        window: 'readonly',
        clearInterval: 'readonly',
        setTimeout: 'readonly',
        setInterval: 'readonly',
        MouseEvent: 'readonly',
      },
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'warn', // any → warn（不阻断）
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      'vue/multi-word-component-names': 'off', // 允许单字组件名
      'no-console': 'warn', // console → warn
    },
  },
)
