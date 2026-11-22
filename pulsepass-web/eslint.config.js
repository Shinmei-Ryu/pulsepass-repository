// ESLint, como pide el PRD de front-end (§16 y QT-FE-010). Es la configuración de la plantilla react-ts de Vite.
import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
    globalIgnores(['dist']),
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            js.configs.recommended,
            tseslint.configs.recommended,
            // Reglas de los hooks y de React Compiler (incluye incompatible-library, refs y set-state-in-effect)
            reactHooks.configs.flat.recommended,
            // Un archivo .tsx que exporta componentes no exporta otras cosas (recarga automática de Vite)
            reactRefresh.configs.vite,
        ],
        languageOptions: {
            ecmaVersion: 2022,
            globals: globals.browser,
        },
    },
])