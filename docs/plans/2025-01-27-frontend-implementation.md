# 物流ERP系统 - 前端开发详细实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标：** 构建物流ERP系统前端应用（Web管理端 + 移动端司机端）

**架构：** Next.js 14 + React 18 + Ant Design + Redux Toolkit + React Native

**技术栈：**
- 前端框架: Next.js 14 (App Router)
- UI组件库: Ant Design 5.x
- 状态管理: Redux Toolkit + RTK Query
- 移动端: React Native + Expo
- 样式: CSS Modules + Less
- 打包: Turborepo

---

## 目录结构

```
gh-transport-frontend/
├── apps/
│   ├── admin/                           # Web管理端
│   │   ├── src/
│   │   │   ├── app/                     # Next.js App Router
│   │   │   │   ├── (auth)/              # 认证布局组
│   │   │   │   │   ├── login/
│   │   │   │   │   │   └── page.tsx
│   │   │   │   │   └── layout.tsx
│   │   │   │   ├── (main)/              # 主布局组
│   │   │   │   │   ├── layout.tsx
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── orders/
│   │   │   │   │   ├── dispatch/
│   │   │   │   │   ├── transport/
│   │   │   │   │   ├── customers/
│   │   │   │   │   └── settings/
│   │   │   │   ├── api/                 # API路由
│   │   │   │   └── globals.css
│   │   │   ├── components/              # 共享组件
│   │   │   │   ├── ui/                  # 基础UI
│   │   │   │   ├── layout/              # 布局组件
│   │   │   │   └── common/              # 通用业务组件
│   │   │   ├── features/                # 功能模块
│   │   │   │   ├── auth/
│   │   │   │   ├── orders/
│   │   │   │   ├── dispatch/
│   │   │   │   ├── transport/
│   │   │   │   └── customers/
│   │   │   ├── hooks/                   # 自定义Hooks
│   │   │   ├── store/                   # Redux Store
│   │   │   ├── services/                # API服务
│   │   │   ├── types/                   # TypeScript类型
│   │   │   └── utils/                   # 工具函数
│   │   │   ├── .env.local
│   │   │   ├── next.config.js
│   │   │   └── tsconfig.json
│   │   └── package.json
│   │
│   └── driver/                          # 移动端司机端
│       ├── src/
│       │   ├── app/                     # Expo Router
│       │   │   ├── (auth)/
│       │   │   │   └── login.tsx
│       │   │   ├── (main)/
│       │   │   │   ├── _layout.tsx
│       │   │   │   ├── home/
│       │   │   │   ├── tasks/
│       │   │   │   ├── scan/
│       │   │   │   └── profile/
│       │   │   └── +html.tsx
│       │   ├── components/              # 共享组件
│       │   ├── features/                # 功能模块
│       │   ├── hooks/                   # 自定义Hooks
│       │   ├── services/                # API服务
│       │   ├── store/                   # Zustand Store
│       │   ├── types/                   # TypeScript类型
│       │   └── utils/                   # 工具函数
│       ├── app.json
│       ├── babel.config.js
│       └── package.json
│
├── packages/
│   ├── ui/                              # 共享UI组件库
│   │   └── src/
│   │       └── index.tsx
│   ├── api/                             # API类型定义
│   │   └── src/
│   │           └── index.ts
│   └── utils/                           # 共享工具
│       └── src/
│           └── index.ts
│
├── package.json
├── turbo.json
└── README.md
```

---

## Phase 1: 项目初始化

### Task 1: 创建Monorepo结构和配置文件

**Files:**
- Create: `package.json`
- Create: `turbo.json`
- Create: `tsconfig.base.json`
- Create: `.eslintrc.json`
- Create: `.prettierrc`

**Step 1: 创建根目录package.json**

```json
{
  "name": "gh-transport-frontend",
  "private": true,
  "workspaces": [
    "apps/*",
    "packages/*"
  ],
  "scripts": {
    "dev": "turbo run dev",
    "dev:admin": "npm run dev --workspace=admin",
    "dev:driver": "npm run dev --workspace=driver",
    "build": "turbo run build",
    "build:admin": "npm run build --workspace=admin",
    "lint": "turbo run lint",
    "format": "prettier --write \"**/*.{ts,tsx,md}\""
  },
  "devDependencies": {
    "turbo": "latest",
    "typescript": "latest",
    "@typescript-eslint/eslint-plugin": "latest",
    "@typescript-eslint/parser": "latest",
    "eslint": "latest",
    "prettier": "latest"
  }
}
```

**Step 2: 创建turbo.json**

```json
{
  "$schema": "https://turbo.build/schema.json",
  "pipeline": {
    "dev": {
      "cache": false,
      "persistent": true
    },
    "build": {
      "dependsOn": ["^build"],
      "outputs": ["dist/**", ".next/**"]
    },
    "lint": {}
  }
}
```

**Step 3: 创建基础tsconfig**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020"],
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "skipLibCheck": true,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "exclude": ["node_modules"]
}
```

**Step 4: Commit**

```bash
git add package.json turbo.json tsconfig.base.json .eslintrc.json .prettierrc
git commit -m "chore: 初始化Monorepo项目结构"
```

---

### Task 2: 初始化Web管理端

**Files:**
- Create: `apps/admin/package.json`
- Create: `apps/admin/tsconfig.json`
- Create: `apps/admin/next.config.js`
- Create: `apps/admin/src/app/layout.tsx`
- Create: `apps/admin/src/app/globals.css`

**Step 1: 创建admin package.json**

```json
{
  "name": "@gh-transport/admin",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "dev": "next dev -p 3001",
    "build": "next build",
    "start": "next start",
    "lint": "next lint"
  },
  "dependencies": {
    "next": "14.1.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "antd": "^5.12.0",
    "@ant-design/icons": "^5.2.6",
    "@ant-design/nextjs-registry": "^1.0.0",
    "@reduxjs/toolkit": "^2.0.1",
    "react-redux": "^9.0.4",
    "axios": "^1.6.2",
    "dayjs": "^1.11.10",
    "lodash": "^4.17.21"
  },
  "devDependencies": {
    "@types/node": "^20.10.0",
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@types/lodash": "^4.14.202",
    "typescript": "^5.3.0",
    "eslint": "^8.55.0",
    "eslint-config-next": "14.1.0",
    "less": "^4.2.0"
  }
}
```

**Step 2: 创建next.config.js**

```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  transpilePackages: ['antd', '@ant-design/icons', '@ant-design/cssinjs'],
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
  webpack(config) {
    config.module.rules.push({
      test: /\.less$/,
      use: [
        'style-loader',
        {
          loader: 'css-loader',
          options: {
            modules: {
              localIdentName: '[local]_[hash:base64:8]',
            },
          },
        },
        'less-loader',
      ],
    });
    return config;
  },
};

module.exports = nextConfig;
```

**Step 3: 创建全局样式**

```css
/* apps/admin/src/app/globals.css */
:root {
  --primary-color: #1890ff;
  --success-color: #52c41a;
  --warning-color: #faad14;
  --error-color: #ff4d4f;
  --font-size-base: 14px;
  --heading-color: rgba(0, 0, 0, 0.85);
  --text-color: rgba(0, 0, 0, 0.65);
  --text-color-secondary: rgba(0, 0, 0, 0.45);
  --disabled-color: rgba(0, 0, 0, 0.25%);
  --border-color-base: #d9d9d9;
  --box-shadow-base: 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
}

* {
  box-sizing: border-box;
  padding: 0;
  margin: 0;
}

html,
body {
  max-width: 100vw;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.ant-layout {
  min-height: 100vh;
}

.ant-layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow: auto;
}

.main-content {
  margin-left: 200px;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
```

**Step 4: 创建根布局**

```tsx
// apps/admin/src/app/layout.tsx
import type { Metadata } from 'next';
import { AntdRegistry } from '@ant-design/nextjs-registry';
import './globals.css';

export const metadata: Metadata = {
  title: 'GH Transport - 物流ERP系统',
  description: '物流ERP管理系统',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN">
      <body>
        <AntdRegistry>{children}</AntdRegistry>
      </body>
    </html>
  );
}
```

**Step 5: Commit**

```bash
git add apps/admin/
git commit -m "feat(admin): 初始化Next.js管理端项目"
```

---

### Task 3: 配置Redux Store

**Files:**
- Create: `apps/admin/src/store/index.ts`
- Create: `apps/admin/src/store/hooks.ts`
- Create: `apps/admin/src/features/auth/store/authSlice.ts`

**Step 1: 创建Store配置**

```typescript
// apps/admin/src/store/index.ts
import { configureStore, combineReducers } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import { ordersApi } from '@/features/orders/api/ordersApi';
import authReducer from '@/features/auth/store/authSlice';
import uiReducer from '@/features/system/store/uiSlice';

const rootReducer = combineReducers({
  auth: authReducer,
  ui: uiReducer,
  [ordersApi.reducerPath]: ordersApi.reducer,
});

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(ordersApi.middleware),
});

setupListeners(store.dispatch);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
```

**Step 2: 创建Typed Hooks**

```typescript
// apps/admin/src/store/hooks.ts
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from './index';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
```

**Step 3: 创建Auth Slice**

```typescript
// apps/admin/src/features/auth/store/authSlice.ts
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';

interface User {
  id: string;
  username: string;
  email: string;
  role: string;
  avatar?: string;
}

interface LoginParams {
  username: string;
  password: string;
}

interface LoginResponse {
  user: User;
  token: string;
  refreshToken: string;
  expiresIn: number;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  status: 'idle' | 'loading' | 'failed';
  error: string | null;
}

const initialState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  status: 'idle',
  error: null,
};

export const loginAsync = createAsyncThunk<
  LoginResponse,
  LoginParams,
  { rejectValue: string }
>('auth/login', async (params, { rejectWithValue }) => {
  try {
    const response = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params),
    });

    if (!response.ok) {
      const error = await response.json();
      return rejectWithValue(error.message || '登录失败');
    }

    return response.json();
  } catch (error) {
    return rejectWithValue('网络错误，请稍后重试');
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action: PayloadAction<LoginResponse>) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.isAuthenticated = true;
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.status = 'idle';
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginAsync.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(loginAsync.fulfilled, (state, action) => {
        state.status = 'idle';
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.isAuthenticated = true;
      })
      .addCase(loginAsync.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload || '登录失败';
      });
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
```

**Step 4: 创建Provider组件**

```tsx
// apps/admin/src/components/Providers.tsx
'use client';

import { Provider } from 'react-redux';
import { store } from '@/store';
import { AntdRegistry } from '@ant-design/nextjs-registry';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <Provider store={store}>
      <AntdRegistry>{children}</AntdRegistry>
    </Provider>
  );
}
```

**Step 5: 更新根布局**

```tsx
// apps/admin/src/app/layout.tsx
import type { Metadata } from 'next';
import { Providers } from '@/components/Providers';
import './globals.css';

export const metadata: Metadata = {
  title: 'GH Transport - 物流ERP系统',
  description: '物流ERP管理系统',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN">
      <body>
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  );
}
```

**Step 6: Commit**

```bash
git add apps/admin/src/store/ apps/admin/src/components/
git commit -m "feat(admin): 配置Redux Store和Auth模块"
```

---

### Task 4: 实现布局组件

**Files:**
- Create: `apps/admin/src/components/layout/Header.tsx`
- Create: `apps/admin/src/components/layout/Sidebar.tsx`
- Create: `apps/admin/src/components/layout/MainLayout.tsx`

**Step 1: 创建Sidebar组件**

```tsx
// apps/admin/src/components/layout/Sidebar.tsx
'use client';

import { Layout, Menu, Typography } from 'antd';
import {
  DashboardOutlined,
  ShoppingCartOutlined,
  CarOutlined,
  TeamOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { useRouter, usePathname } from 'next/navigation';
import type { MenuProps } from 'antd';

const { Sider } = Layout;
const { Title } = Typography;

const menuItems: MenuProps['items'] = [
  {
    key: '/dashboard',
    icon: <DashboardOutlined />,
    label: '仪表盘',
  },
  {
    key: '/orders',
    icon: <ShoppingCartOutlined />,
    label: '订单管理',
  },
  {
    key: '/dispatch',
    icon: <CarOutlined />,
    label: '调度管理',
  },
  {
    key: '/transport',
    icon: <TeamOutlined />,
    label: '运输管理',
    children: [
      { key: '/transport/vehicles', label: '车辆管理' },
      { key: '/transport/drivers', label: '司机管理' },
    ],
  },
  {
    key: '/customers',
    icon: <TeamOutlined />,
    label: '客户管理',
  },
  {
    key: '/settings',
    icon: <SettingOutlined />,
    label: '系统设置',
  },
];

export const Sidebar = () => {
  const router = useRouter();
  const pathname = usePathname();

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    router.push(key);
  };

  return (
    <Sider
      width={200}
      theme="light"
      style={{
        borderRight: '1px solid #f0f0f0',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        zIndex: 100,
      }}
    >
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: '1px solid #f0f0f0',
        }}
      >
        <Title level={4} style={{ margin: 0, color: '#1890ff' }}>
          GH Transport
        </Title>
      </div>
      <Menu
        mode="inline"
        selectedKeys={[pathname]}
        items={menuItems}
        onClick={handleMenuClick}
        style={{ borderRight: 0, marginTop: 8 }}
      />
    </Sider>
  );
};
```

**Step 2: 创建Header组件**

```tsx
// apps/admin/src/components/layout/Header.tsx
'use client';

import { Layout, Avatar, Dropdown, Space, Button, Badge } from 'antd';
import {
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { logout } from '@/features/auth/store/authSlice';
import { useRouter } from 'next/navigation';

const { Header: AntHeader } = Layout;

export const Header = () => {
  const dispatch = useAppDispatch();
  const router = useRouter();
  const { user } = useAppSelector((state) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    router.push('/login');
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  return (
    <AntHeader
      style={{
        background: '#fff',
        padding: '0 24px',
        display: 'flex',
        justifyContent: 'flex-end',
        alignItems: 'center',
        borderBottom: '1px solid #f0f0f0',
        position: 'fixed',
        right: 0,
        top: 0,
        left: 200,
        zIndex: 99,
        width: 'calc(100% - 200px)',
      }}
    >
      <Space size={24}>
        <Badge count={3}>
          <Button type="text" icon={<BellOutlined style={{ fontSize: 18 }} />} />
        </Badge>

        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Space style={{ cursor: 'pointer' }}>
            <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }} />
            <span>{user?.username || '用户'}</span>
          </Space>
        </Dropdown>
      </Space>
    </AntHeader>
  );
};
```

**Step 3: 创建主布局组件**

```tsx
// apps/admin/src/components/layout/MainLayout.tsx
'use client';

import { Layout } from 'antd';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout = ({ children }: MainLayoutProps) => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sidebar />
      <Layout style={{ marginLeft: 200 }}>
        <Header />
        <main
          style={{
            marginTop: 64,
            padding: 24,
            minHeight: 'calc(100vh - 64px)',
            background: '#f5f5f5',
          }}
        >
          {children}
        </main>
      </Layout>
    </Layout>
  );
};
```

**Step 4: 创建登录页**

```tsx
// apps/admin/src/app/(auth)/login/page.tsx
'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { loginAsync } from '@/features/auth/store/authSlice';
import { useRouter } from 'next/navigation';

const { Title, Text } = Typography;

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const dispatch = useAppDispatch();
  const router = useRouter();
  const { status, error } = useAppSelector((state) => state.auth);

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const result = await dispatch(loginAsync(values)).unwrap();
      message.success('登录成功');
      router.push('/dashboard');
    } catch (err) {
      message.error(err as string || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card style={{ width: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ margin: 0, color: '#1890ff' }}>
            GH Transport
          </Title>
          <Text type="secondary">物流ERP管理系统</Text>
        </div>

        <Form name="login" onFinish={onFinish} size="large">
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              style={{ height: 44 }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
```

**Step 5: Commit**

```bash
git add apps/admin/src/components/layout/ apps/admin/src/app/\(auth\)/
git commit -m "feat(admin): 实现登录页和主布局组件"
```

---

## Phase 2: 订单管理模块

### Task 5: 创建订单类型定义和API

**Files:**
- Create: `apps/admin/src/types/order.ts`
- Create: `apps/admin/src/features/orders/api/ordersApi.ts`

**Step 1: 创建订单类型定义**

```typescript
// apps/admin/src/types/order.ts
export type OrderStatus =
  | 'PENDING'
  | 'DISPATCHED'
  | 'IN_TRANSIT'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXCEPTION';

export type OrderStatusText = {
  [key in OrderStatus]: string;
};

export const ORDER_STATUS_TEXT: OrderStatusText = {
  PENDING: '待调度',
  DISPATCHED: '已调度',
  IN_TRANSIT: '运输中',
  DELIVERED: '已送达',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  EXCEPTION: '异常',
};

export interface Address {
  province: string;
  city: string;
  district: string;
  detail: string;
  contactName: string;
  contactPhone: string;
  fullAddress?: string;
}

export interface OrderItem {
  id: string;
  itemName: string;
  quantity: number;
  weight: number;
  volume: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: string;
  orderNo: string;
  customerId: string;
  customerName: string;
  status: OrderStatus;
  pickupAddress: Address;
  deliveryAddress: Address;
  totalAmount: number;
  currency: string;
  items: OrderItem[];
  pickupTime: string;
  deliveryTime: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderListQuery {
  status?: OrderStatus;
  customerId?: string;
  startDate?: string;
  endDate?: string;
  pageNum: number;
  pageSize: number;
}

export interface OrderListResponse {
  items: Order[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface CreateOrderRequest {
  customerId: string;
  pickupAddress: Address;
  deliveryAddress: Address;
  items: CreateOrderItemRequest[];
  pickupTime: string;
  deliveryTime: string;
}

export interface CreateOrderItemRequest {
  itemName: string;
  quantity: number;
  weight: number;
  volume: number;
  unitPrice: number;
}

export interface CancelOrderRequest {
  orderId: string;
  reason: string;
}
```

**Step 2: 创建RTK Query API**

```typescript
// apps/admin/src/features/orders/api/ordersApi.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type {
  Order,
  OrderListQuery,
  OrderListResponse,
  CreateOrderRequest,
  CancelOrderRequest,
} from '@/types/order';

export const ordersApi = createApi({
  reducerPath: 'ordersApi',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api/v1/orders',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as { auth: { token: string | null } }).auth.token;
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Order'],
  endpoints: (builder) => ({
    // 查询订单列表
    getOrders: builder.query<OrderListResponse, OrderListQuery>({
      query: (params) => {
        const searchParams = new URLSearchParams();
        if (params.status) searchParams.set('status', params.status);
        if (params.customerId) searchParams.set('customerId', params.customerId);
        if (params.startDate) searchParams.set('startDate', params.startDate);
        if (params.endDate) searchParams.set('endDate', params.endDate);
        searchParams.set('pageNum', String(params.pageNum));
        searchParams.set('pageSize', String(params.pageSize));
        return `?${searchParams.toString()}`;
      },
      providesTags: (result) =>
        result
          ? [
              ...result.items.map(({ id }) => ({ type: 'Order' as const, id })),
              { type: 'Order', id: 'LIST' },
            ]
          : [{ type: 'Order', id: 'LIST' }],
    }),

    // 获取订单详情
    getOrder: builder.query<Order, string>({
      query: (id) => `/${id}`,
      providesTags: (_, __, id) => [{ type: 'Order', id }],
    }),

    // 创建订单
    createOrder: builder.mutation<Order, CreateOrderRequest>({
      query: (body) => ({
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'Order', id: 'LIST' }],
    }),

    // 取消订单
    cancelOrder: builder.mutation<void, CancelOrderRequest>({
      query: ({ orderId, reason }) => ({
        url: `/${orderId}/cancel`,
        method: 'POST',
        body: { reason },
      }),
      invalidatesTags: (_, __, { orderId }) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),

    // 标记已调度
    markDispatched: builder.mutation<void, { orderId: string; dispatchId: string }>({
      query: ({ orderId, dispatchId }) => ({
        url: `/${orderId}/dispatch`,
        method: 'POST',
        body: { dispatchId },
      }),
      invalidatesTags: (_, __, { orderId }) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),

    // 开始运输
    startTransport: builder.mutation<void, string>({
      query: (orderId) => ({
        url: `/${orderId}/start`,
        method: 'POST',
      }),
      invalidatesTags: (_, __, orderId) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),

    // 确认送达
    markDelivered: builder.mutation<void, string>({
      query: (orderId) => ({
        url: `/${orderId}/deliver`,
        method: 'POST',
      }),
      invalidatesTags: (_, __, orderId) => [
        { type: 'Order', id: orderId },
        { type: 'Order', id: 'LIST' },
      ],
    }),
  }),
});

export const {
  useGetOrdersQuery,
  useGetOrderQuery,
  useCreateOrderMutation,
  useCancelOrderMutation,
  useMarkDispatchedMutation,
  useStartTransportMutation,
  useMarkDeliveredMutation,
} = ordersApi;
```

**Step 3: 创建Store集成**

```typescript
// apps/admin/src/store/index.ts (更新)
import { ordersApi } from '@/features/orders/api/ordersApi';

// 在 middleware 中添加
middleware: (getDefaultMiddleware) =>
  getDefaultMiddleware().concat(ordersApi.middleware),

// 在 reducer 中添加
[ordersApi.reducerPath]: ordersApi.reducer,
```

**Step 4: Commit**

```bash
git add apps/admin/src/types/ apps/admin/src/features/orders/api/
git commit -m "feat(orders): 创建订单类型定义和RTK Query API"
```

---

### Task 6: 实现订单列表页

**Files:**
- Create: `apps/admin/src/features/orders/components/OrderList/index.tsx`
- Create: `apps/admin/src/features/orders/components/OrderFilter/index.tsx`
- Create: `apps/admin/src/features/orders/components/OrderStatusTag/index.tsx`

**Step 1: 创建订单状态标签组件**

```tsx
// apps/admin/src/features/orders/components/OrderStatusTag/index.tsx
import { Tag } from 'antd';
import type { OrderStatus, ORDER_STATUS_TEXT } from '@/types/order';

interface Props {
  status: OrderStatus;
}

const statusColorMap: Record<OrderStatus, string> = {
  PENDING: 'gold',
  DISPATCHED: 'blue',
  IN_TRANSIT: 'cyan',
  DELIVERED: 'green',
  COMPLETED: 'default',
  CANCELLED: 'red',
  EXCEPTION: 'orange',
};

export const OrderStatusTag = ({ status }: Props) => {
  const color = statusColorMap[status] || 'default';
  const text = (ORDER_STATUS_TEXT as Record<OrderStatus, string>)[status] || status;
  return <Tag color={color}>{text}</Tag>;
};
```

**Step 2: 创建订单筛选组件**

```tsx
// apps/admin/src/features/orders/components/OrderFilter/index.tsx
import { Form, Input, Select, DatePicker, Button, Space } from 'antd';
import type { OrderStatus } from '@/types/order';
import { Search, ClearOutlined } from '@ant-design/icons';

interface OrderFilterProps {
  onSearch: (values: Record<string, unknown>) => void;
}

const { RangePicker } = DatePicker;

export const OrderFilter = ({ onSearch }: OrderFilterProps) => {
  const [form] = Form.useForm();

  const handleReset = () => {
    form.resetFields();
    onSearch({});
  };

  return (
    <Form
      form={form}
      layout="inline"
      onFinish={onSearch}
      style={{ marginBottom: 16, flexWrap: 'wrap', gap: 12 }}
    >
      <Form.Item name="orderNo">
        <Input placeholder="订单号" style={{ width: 160 }} allowClear />
      </Form.Item>

      <Form.Item name="status">
        <Select
          placeholder="订单状态"
          style={{ width: 120 }}
          allowClear
          options={[
            { value: 'PENDING', label: '待调度' },
            { value: 'DISPATCHED', label: '已调度' },
            { value: 'IN_TRANSIT', label: '运输中' },
            { value: 'DELIVERED', label: '已送达' },
            { value: 'COMPLETED', label: '已完成' },
            { value: 'CANCELLED', label: '已取消' },
          ]}
        />
      </Form.Item>

      <Form.Item name="dateRange">
        <RangePicker placeholder={['开始日期', '结束日期']} />
      </Form.Item>

      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" icon={<Search />}>
            搜索
          </Button>
          <Button onClick={handleReset} icon={<ClearOutlined />}>
            重置
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};
```

**Step 3: 创建订单列表组件**

```tsx
// apps/admin/src/features/orders/components/OrderList/index.tsx
'use client';

import { useState } from 'react';
import { Table, Button, Space, Card, Typography, message } from 'antd';
import { EyeOutlined, DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { useGetOrdersQuery, useCancelOrderMutation } from '@/features/orders/api/ordersApi';
import { OrderStatusTag } from '../OrderStatusTag';
import type { Order, OrderListQuery } from '@/types/order';
import { useRouter } from 'next/navigation';

const { Title } = Typography;

interface OrderListProps {
  onCreate?: () => void;
}

export const OrderList = ({ onCreate }: OrderListProps) => {
  const router = useRouter();
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });
  const [filters, setFilters] = useState<Record<string, unknown>>({});

  const { data, isLoading, refetch } = useGetOrdersQuery({
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    ...filters,
  } as OrderListQuery);

  const [cancelOrder] = useCancelOrderMutation();

  const handleTableChange = (newPagination: { current: number; pageSize: number }) => {
    setPagination(newPagination);
  };

  const handleSearch = (values: Record<string, unknown>) => {
    setFilters(values);
    setPagination({ ...pagination, current: 1 });
  };

  const handleCancel = async (orderId: string) => {
    try {
      await cancelOrder({ orderId, reason: '用户取消' }).unwrap();
      message.success('订单已取消');
      refetch();
    } catch (error) {
      message.error('取消失败');
    }
  };

  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 160,
      render: (text: string, record: Order) => (
        <Button
          type="link"
          onClick={() => router.push(`/orders/${record.id}`)}
        >
          {text}
        </Button>
      ),
    },
    {
      title: '客户',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => <OrderStatusTag status={status as any} />,
    },
    {
      title: '取件地址',
      dataIndex: ['pickupAddress', 'city'],
      key: 'pickupCity',
      width: 100,
      render: (_: unknown, record: Order) =>
        `${record.pickupAddress.province}${record.pickupAddress.city}`,
    },
    {
      title: '送达地址',
      dataIndex: ['deliveryAddress', 'city'],
      key: 'deliveryCity',
      width: 100,
      render: (_: unknown, record: Order) =>
        `${record.deliveryAddress.province}${record.deliveryAddress.city}`,
    },
    {
      title: '金额',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      width: 100,
      render: (amount: number, record: Order) =>
        `¥${amount?.toFixed(2) || '0.00'} ${record.currency}`,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      sorter: true,
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 150,
      render: (_: unknown, record: Order) => (
        <Space size="small">
          <Button
            type="text"
            icon={<EyeOutlined />}
            onClick={() => router.push(`/orders/${record.id}`)}
          >
            详情
          </Button>
          {record.status === 'PENDING' && (
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleCancel(record.id)}
            >
              取消
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <Card>
      <div className="page-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={4} style={{ margin: 0 }}>订单管理</Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>
            创建订单
          </Button>
        </div>
      </div>

      <OrderFilter onSearch={handleSearch} />

      <Table
        columns={columns}
        dataSource={data?.items}
        loading={isLoading}
        rowKey="id"
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: data?.total || 0,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        onChange={handleTableChange as any}
        scroll={{ x: 1200 }}
      />
    </Card>
  );
};
```

**Step 4: 创建订单列表页面**

```tsx
// apps/admin/src/app/(main)/orders/page.tsx
'use client';

import { OrderList } from '@/features/orders/components/OrderList';
import { useRouter } from 'next/navigation';

export default function OrdersPage() {
  const router = useRouter();

  const handleCreate = () => {
    router.push('/orders/create');
  };

  return <OrderList onCreate={handleCreate} />;
}
```

**Step 5: Commit**

```bash
git add apps/admin/src/features/orders/components/ apps/admin/src/app/\(main\)/orders/
git commit -m "feat(orders): 实现订单列表页"
```

---

### Task 7: 实现创建订单表单

**Files:**
- Create: `apps/admin/src/features/orders/components/OrderForm/index.tsx`
- Create: `apps/admin/src/app/(main)/orders/create/page.tsx`

**Step 1: 创建订单表单组件**

```tsx
// apps/admin/src/features/orders/components/OrderForm/index.tsx
'use client';

import { useState } from 'react';
import { Form, Input, DatePicker, Button, Card, Row, Col, Space, Table, InputNumber, message, Typography } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import type { CreateOrderRequest, CreateOrderItemRequest } from '@/types/order';
import { useCreateOrderMutation } from '@/features/orders/api/ordersApi';
import { useRouter } from 'next/navigation';
import dayjs from 'dayjs';

const { Title } = Typography;

interface OrderFormProps {
  initialValues?: Record<string, unknown>;
  onSuccess?: () => void;
}

interface ItemFormData {
  itemName: string;
  quantity: number;
  weight: number;
  volume: number;
  unitPrice: number;
}

export const OrderForm = ({ initialValues, onSuccess }: OrderFormProps) => {
  const [form] = Form.useForm();
  const [items, setItems] = useState<ItemFormData[]>([]);
  const [createOrder, { isLoading }] = useCreateOrderMutation();
  const router = useRouter();

  const handleAddItem = () => {
    setItems([...items, { itemName: '', quantity: 1, weight: 0, volume: 0, unitPrice: 0 }]);
  };

  const handleRemoveItem = (index: number) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const handleFinish = async (values: Record<string, unknown>) => {
    if (items.length === 0) {
      message.error('请添加货物明细');
      return;
    }

    const payload: CreateOrderRequest = {
      customerId: values.customerId as string,
      pickupAddress: {
        province: values.pickupProvince as string,
        city: values.pickupCity as string,
        district: values.pickupDistrict as string,
        detail: values.pickupDetail as string,
        contactName: values.pickupContact as string,
        contactPhone: values.pickupPhone as string,
      },
      deliveryAddress: {
        province: values.deliveryProvince as string,
        city: values.deliveryCity as string,
        district: values.deliveryDistrict as string,
        detail: values.deliveryDetail as string,
        contactName: values.deliveryContact as string,
        contactPhone: values.deliveryPhone as string,
      },
      items: items as CreateOrderItemRequest[],
      pickupTime: (values.pickupTime as dayjs.Dayjs).toISOString(),
      deliveryTime: (values.deliveryTime as dayjs.Dayjs).toISOString(),
    };

    try {
      await createOrder(payload).unwrap();
      message.success('订单创建成功');
      router.push('/orders');
    } catch (error) {
      message.error('创建失败');
    }
  };

  const itemColumns = [
    { title: '货物名称', dataIndex: 'itemName', width: 150 },
    { title: '数量', dataIndex: 'quantity', width: 100 },
    { title: '重量(kg)', dataIndex: 'weight', width: 100 },
    { title: '体积(m³)', dataIndex: 'volume', width: 100 },
    { title: '单价(元)', dataIndex: 'unitPrice', width: 100 },
    {
      title: '操作',
      width: 80,
      render: (_: unknown, __: unknown, index: number) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleRemoveItem(index)}
        />
      ),
    },
  ];

  return (
    <Card>
      <div className="page-header">
        <Title level={4} style={{ margin: 0 }}>创建订单</Title>
      </div>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleFinish}
        initialValues={initialValues}
      >
        <Row gutter={24}>
          <Col span={12}>
            <Card title="客户信息" size="small">
              <Form.Item
                name="customerId"
                label="客户ID"
                rules={[{ required: true, message: '请输入客户ID' }]}
              >
                <Input placeholder="请输入客户ID" />
              </Form.Item>
            </Card>
          </Col>

          <Col span={12}>
            <Card title="时间要求" size="small">
              <Form.Item
                name="pickupTime"
                label="取件时间"
                rules={[{ required: true, message: '请选择取件时间' }]}
              >
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item
                name="deliveryTime"
                label="送达时间"
                rules={[{ required: true, message: '请选择送达时间' }]}
              >
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Card>
          </Col>
        </Row>

        <Row gutter={24} style={{ marginTop: 16 }}>
          <Col span={12}>
            <Card title="取件地址" size="small">
              <Form.Item name="pickupProvince" label="省份" rules={[{ required: true }]}>
                <Input placeholder="省份" />
              </Form.Item>
              <Form.Item name="pickupCity" label="城市" rules={[{ required: true }]}>
                <Input placeholder="城市" />
              </Form.Item>
              <Form.Item name="pickupDistrict" label="区县">
                <Input placeholder="区县" />
              </Form.Item>
              <Form.Item name="pickupDetail" label="详细地址" rules={[{ required: true }]}>
                <Input.TextArea rows={2} placeholder="详细地址" />
              </Form.Item>
              <Row gutter={8}>
                <Col span={12}>
                  <Form.Item name="pickupContact" label="联系人">
                    <Input placeholder="联系人" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="pickupPhone" label="联系电话">
                    <Input placeholder="联系电话" />
                  </Form.Item>
                </Col>
              </Row>
            </Card>
          </Col>

          <Col span={12}>
            <Card title="送达地址" size="small">
              <Form.Item name="deliveryProvince" label="省份" rules={[{ required: true }]}>
                <Input placeholder="省份" />
              </Form.Item>
              <Form.Item name="deliveryCity" label="城市" rules={[{ required: true }]}>
                <Input placeholder="城市" />
              </Form.Item>
              <Form.Item name="deliveryDistrict" label="区县">
                <Input placeholder="区县" />
              </Form.Item>
              <Form.Item name="deliveryDetail" label="详细地址" rules={[{ required: true }]}>
                <Input.TextArea rows={2} placeholder="详细地址" />
              </Form.Item>
              <Row gutter={8}>
                <Col span={12}>
                  <Form.Item name="deliveryContact" label="联系人">
                    <Input placeholder="联系人" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="deliveryPhone" label="联系电话">
                    <Input placeholder="联系电话" />
                  </Form.Item>
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>

        <Card title="货物明细" size="small" style={{ marginTop: 16 }}>
          <Table
            dataSource={items}
            columns={itemColumns}
            rowKey={(_, index) => index as string}
            pagination={false}
            size="small"
          />
          <Button
            type="dashed"
            onClick={handleAddItem}
            block
            style={{ marginTop: 16 }}
            icon={<PlusOutlined />}
          >
            添加货物
          </Button>
        </Card>

        <Form.Item style={{ marginTop: 24 }}>
          <Space>
            <Button type="primary" htmlType="submit" loading={isLoading}>
              提交订单
            </Button>
            <Button onClick={() => router.back()}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};
```

**Step 2: 创建创建订单页面**

```tsx
// apps/admin/src/app/(main)/orders/create/page.tsx
'use client';

import { OrderForm } from '@/features/orders/components/OrderForm';

export default function CreateOrderPage() {
  return <OrderForm />;
}
```

**Step 3: Commit**

```bash
git add apps/admin/src/features/orders/components/OrderForm/ apps/admin/src/app/\(main\)/orders/create/
git commit -m "feat(orders): 实现创建订单表单"
```

---

### Task 8: 实现订单详情页

**Files:**
- Create: `apps/admin/src/features/orders/components/OrderDetail/index.tsx`
- Create: `apps/admin/src/app/(main)/orders/[id]/page.tsx`

**Step 1: 创建订单详情组件**

```tsx
// apps/admin/src/features/orders/components/OrderDetail/index.tsx
'use client';

import { use, useState } from 'react';
import { Card, Descriptions, Table, Button, Space, Timeline, Typography, Row, Col, Spin, message } from 'antd';
import {
  ArrowLeftOutlined,
  PrinterOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useGetOrderQuery, useStartTransportMutation, useMarkDeliveredMutation } from '@/features/orders/api/ordersApi';
import { OrderStatusTag } from '../OrderStatusTag';
import type { Order } from '@/types/order';
import { useRouter } from 'next/navigation';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

interface OrderDetailProps {
  orderId: string;
}

export const OrderDetail = ({ orderId }: OrderDetailProps) => {
  const router = useRouter();
  const { data: order, isLoading, refetch } = useGetOrderQuery(orderId);
  const [startTransport, { isLoading: startLoading }] = useStartTransportMutation();
  const [markDelivered, { isLoading: deliverLoading }] = useMarkDeliveredMutation();

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!order) {
    return <div>订单不存在</div>;
  }

  const handleStartTransport = async () => {
    try {
      await startTransport(orderId).unwrap();
      message.success('已开始运输');
      refetch();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleMarkDelivered = async () => {
    try {
      await markDelivered(orderId).unwrap();
      message.success('已确认送达');
      refetch();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const itemColumns = [
    { title: '货物名称', dataIndex: 'itemName', key: 'itemName' },
    { title: '数量', dataIndex: 'quantity', key: 'quantity' },
    { title: '重量(kg)', dataIndex: 'weight', key: 'weight' },
    { title: '体积(m³)', dataIndex: 'volume', key: 'volume' },
    { title: '单价(元)', dataIndex: 'unitPrice', key: 'unitPrice' },
    { title: '小计(元)', dataIndex: 'subtotal', key: 'subtotal' },
  ];

  const addressColumns = [
    { title: '省份', dataIndex: 'province', key: 'province' },
    { title: '城市', dataIndex: 'city', key: 'city' },
    { title: '区县', dataIndex: 'district', key: 'district' },
    { title: '详细地址', dataIndex: 'detail', key: 'detail' },
    { title: '联系人', dataIndex: 'contactName', key: 'contactName' },
    { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone' },
  ];

  return (
    <Card
      title={
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()} />
          <span>订单详情</span>
        </Space>
      }
      extra={
        <Space>
          <Button icon={<PrinterOutlined />}>打印</Button>
          {order.status === 'DISPATCHED' && (
            <Button
              type="primary"
              icon={<CheckCircleOutlined />}
              onClick={handleStartTransport}
              loading={startLoading}
            >
              开始运输
            </Button>
          )}
          {order.status === 'IN_TRANSIT' && (
            <Button
              type="primary"
              icon={<CheckCircleOutlined />}
              onClick={handleMarkDelivered}
              loading={deliverLoading}
            >
              确认送达
            </Button>
          )}
        </Space>
      }
    >
      <Descriptions bordered column={2}>
        <Descriptions.Item label="订单号">{order.orderNo}</Descriptions.Item>
        <Descriptions.Item label="状态">
          <OrderStatusTag status={order.status} />
        </Descriptions.Item>
        <Descriptions.Item label="客户ID">{order.customerId}</Descriptions.Item>
        <Descriptions.Item label="客户名称">{order.customerName}</Descriptions.Item>
        <Descriptions.Item label="总金额">
          ¥{order.totalAmount?.toFixed(2)} {order.currency}
        </Descriptions.Item>
        <Descriptions.Item label="创建时间">
          {dayjs(order.createdAt).format('YYYY-MM-DD HH:mm:ss')}
        </Descriptions.Item>
      </Descriptions>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={12}>
          <Card title="取件地址" size="small">
            <Table
              dataSource={[order.pickupAddress]}
              columns={addressColumns}
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="送达地址" size="small">
            <Table
              dataSource={[order.deliveryAddress]}
              columns={addressColumns}
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>

      <Card title="货物明细" size="small" style={{ marginTop: 16 }}>
        <Table
          dataSource={order.items}
          columns={itemColumns}
          rowKey="id"
          pagination={false}
          size="small"
        />
      </Card>

      <Card title="订单时间线" size="small" style={{ marginTop: 16 }}>
        <Timeline
          items={[
            {
              color: 'green',
              children: `订单创建 - ${dayjs(order.createdAt).format('YYYY-MM-DD HH:mm:ss')}`,
            },
            order.status !== 'PENDING' && {
              color: 'blue',
              children: `已调度 - ${dayjs(order.updatedAt).format('YYYY-MM-DD HH:mm:ss')}`,
            },
            (order.status === 'IN_TRANSIT' || order.status === 'DELIVERED' || order.status === 'COMPLETED') && {
              color: 'cyan',
              children: `开始运输 - ${dayjs(order.updatedAt).format('YYYY-MM-DD HH:mm:ss')}`,
            },
            (order.status === 'DELIVERED' || order.status === 'COMPLETED') && {
              color: 'green',
              children: `已送达 - ${dayjs(order.updatedAt).format('YYYY-MM-DD HH:mm:ss')}`,
            },
            order.status === 'COMPLETED' && {
              color: 'green',
              children: `已完成 - ${dayjs(order.updatedAt).format('YYYY-MM-DD HH:mm:ss')}`,
            },
            order.status === 'CANCELLED' && {
              color: 'red',
              children: `已取消 - ${dayjs(order.updatedAt).format('YYYY-MM-DD HH:mm:ss')}`,
            },
          ].filter(Boolean) as any}
        />
      </Card>
    </Card>
  );
};
```

**Step 2: 创建订单详情页面**

```tsx
// apps/admin/src/app/(main)/orders/[id]/page.tsx
'use client';

import { OrderDetail } from '@/features/orders/components/OrderDetail';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function OrderDetailPage({ params }: PageProps) {
  const { id } = use(params);
  return <OrderDetail orderId={id} />;
}
```

**Step 3: Commit**

```bash
git add apps/admin/src/features/orders/components/OrderDetail/ apps/admin/src/app/\(main\)/orders/\[id\]/
git commit -m "feat(orders): 实现订单详情页"
```

---

## Phase 3: 调度管理模块

### Task 9: 实现调度管理模块

**Files:**
- Create: `apps/admin/src/types/dispatch.ts`
- Create: `apps/admin/src/features/dispatch/api/dispatchApi.ts`
- Create: `apps/admin/src/features/dispatch/components/DispatchBoard/index.tsx`
- Create: `apps/admin/src/app/(main)/dispatch/page.tsx`

**Step 1: 创建调度类型定义**

```typescript
// apps/admin/src/types/dispatch.ts
export type DispatchStatus =
  | 'CREATED'
  | 'DRIVER_ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export const DISPATCH_STATUS_TEXT: Record<DispatchStatus, string> = {
  CREATED: '待分配',
  DRIVER_ASSIGNED: '已分配司机',
  IN_PROGRESS: '执行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

export interface Dispatch {
  id: string;
  dispatchNo: string;
  orderId: string;
  orderNo: string;
  vehicleId: string;
  vehiclePlateNumber: string;
  driverId: string;
  driverName: string;
  driverPhone: string;
  status: DispatchStatus;
  plannedTime: {
    start: string;
    end: string;
  };
  actualStartTime?: string;
  actualEndTime?: string;
  createdAt: string;
}

export interface DispatchListQuery {
  status?: DispatchStatus;
  vehicleId?: string;
  driverId?: string;
  startDate?: string;
  endDate?: string;
  pageNum: number;
  pageSize: number;
}

export interface CreateDispatchRequest {
  orderId: string;
  vehicleId: string;
  driverId: string;
  plannedStartTime: string;
  plannedEndTime: string;
}
```

**Step 2: 创建调度API**

```typescript
// apps/admin/src/features/dispatch/api/dispatchApi.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { Dispatch, DispatchListQuery, CreateDispatchRequest } from '@/types/dispatch';

export const dispatchApi = createApi({
  reducerPath: 'dispatchApi',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api/v1/dispatches',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as { auth: { token: string | null } }).auth.token;
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Dispatch'],
  endpoints: (builder) => ({
    getDispatches: builder.query<{ items: Dispatch[]; total: number }, DispatchListQuery>({
      query: (params) => ({ url: '', params }),
      providesTags: [{ type: 'Dispatch', id: 'LIST' }],
    }),
    getDispatch: builder.query<Dispatch, string>({
      query: (id) => `/${id}`,
      providesTags: (_, __, id) => [{ type: 'Dispatch', id }],
    }),
    createDispatch: builder.mutation<Dispatch, CreateDispatchRequest>({
      query: (body) => ({ method: 'POST', body }),
      invalidatesTags: [{ type: 'Dispatch', id: 'LIST' }],
    }),
    startDispatch: builder.mutation<void, string>({
      query: (id) => ({ url: `/${id}/start`, method: 'POST' }),
      invalidatesTags: (_, __, id) => [{ type: 'Dispatch', id }, { type: 'Dispatch', id: 'LIST' }],
    }),
    completeDispatch: builder.mutation<void, string>({
      query: (id) => ({ url: `/${id}/complete`, method: 'POST' }),
      invalidatesTags: (_, __, id) => [{ type: 'Dispatch', id }, { type: 'Dispatch', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetDispatchesQuery,
  useGetDispatchQuery,
  useCreateDispatchMutation,
  useStartDispatchMutation,
  useCompleteDispatchMutation,
} = dispatchApi;
```

**Step 3: 创建调度看板组件**

```tsx
// apps/admin/src/features/dispatch/components/DispatchBoard/index.tsx
'use client';

import { useState } from 'react';
import { Card, Table, Tag, Button, Space, Typography, Row, Col, Statistic, message } from 'antd';
import {
  CarOutlined,
  UserOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useGetDispatchesQuery, useStartDispatchMutation } from '@/features/dispatch/api/dispatchApi';
import type { Dispatch, DispatchStatus } from '@/types/dispatch';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const statusColorMap: Record<DispatchStatus, string> = {
  CREATED: 'gold',
  DRIVER_ASSIGNED: 'blue',
  IN_PROGRESS: 'cyan',
  COMPLETED: 'green',
  CANCELLED: 'red',
};

export const DispatchBoard = () => {
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });
  const { data, isLoading, refetch } = useGetDispatchesQuery({
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
  });
  const [startDispatch] = useStartDispatchMutation();

  const columns = [
    {
      title: '调度单号',
      dataIndex: 'dispatchNo',
      key: 'dispatchNo',
      width: 160,
    },
    {
      title: '关联订单',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 160,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: DispatchStatus) => {
        const statusMap: Record<string, { color: string; text: string }> = {
          CREATED: { color: 'gold', text: '待分配' },
          DRIVER_ASSIGNED: { color: 'blue', text: '已分配' },
          IN_PROGRESS: { color: 'cyan', text: '执行中' },
          COMPLETED: { color: 'green', text: '已完成' },
          CANCELLED: { color: 'red', text: '已取消' },
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '车辆',
      dataIndex: 'vehiclePlateNumber',
      key: 'vehicle',
      width: 120,
    },
    {
      title: '司机',
      key: 'driver',
      width: 120,
      render: (_: unknown, record: Dispatch) => (
        <Space>
          <UserOutlined />
          {record.driverName}
        </Space>
      ),
    },
    {
      title: '计划时间',
      key: 'plannedTime',
      width: 200,
      render: (_: unknown, record: Dispatch) => (
        <Text type="secondary">
          {dayjs(record.plannedTime.start).format('MM-DD HH:mm')} - {dayjs(record.plannedTime.end).format('HH:mm')}
        </Text>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_: unknown, record: Dispatch) => (
        <Space>
          {record.status === 'DRIVER_ASSIGNED' && (
            <Button
              type="primary"
              size="small"
              onClick={() => handleStart(record.id)}
            >
              开始执行
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const handleStart = async (dispatchId: string) => {
    try {
      await startDispatch(dispatchId).unwrap();
      message.success('已开始执行');
      refetch();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const stats = {
    total: data?.total || 0,
    pending: data?.items?.filter(d => d.status === 'CREATED').length || 0,
    inProgress: data?.items?.filter(d => d.status === 'IN_PROGRESS').length || 0,
    completed: data?.items?.filter(d => d.status === 'COMPLETED').length || 0,
  };

  return (
    <Card>
      <div className="page-header">
        <Title level={4} style={{ margin: 0 }}>调度管理</Title>
      </div>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Statistic title="今日调度" value={stats.total} prefix={<CarOutlined />} />
        </Col>
        <Col span={6}>
          <Statistic title="待执行" value={stats.pending} valueStyle={{ color: '#faad14' }} />
        </Col>
        <Col span={6}>
          <Statistic title="执行中" value={stats.inProgress} valueStyle={{ color: '#1890ff' }} />
        </Col>
        <Col span={6}>
          <Statistic title="已完成" value={stats.completed} valueStyle={{ color: '#52c41a' }} />
        </Col>
      </Row>

      <Table
        columns={columns}
        dataSource={data?.items}
        loading={isLoading}
        rowKey="id"
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: data?.total || 0,
          showSizeChanger: true,
        }}
        onChange={(p) => setPagination(p as any)}
        scroll={{ x: 1200 }}
      />
    </Card>
  );
};
```

**Step 4: 创建调度管理页面**

```tsx
// apps/admin/src/app/(main)/dispatch/page.tsx
'use client';

import { DispatchBoard } from '@/features/dispatch/components/DispatchBoard';

export default function DispatchPage() {
  return <DispatchBoard />;
}
```

**Step 5: Commit**

```bash
git add apps/admin/src/types/dispatch.ts apps/admin/src/features/dispatch/ apps/admin/src/app/\(main\)/dispatch/
git commit -m "feat(dispatch): 实现调度管理模块"
```

---

## Phase 4: 运输管理模块

### Task 10: 实现运输管理模块

**Files:**
- Create: `apps/admin/src/types/transport.ts`
- Create: `apps/admin/src/features/transport/api/vehicleApi.ts`
- Create: `apps/admin/src/features/transport/components/VehicleList/index.tsx`
- Create: `apps/admin/src/app/(main)/transport/vehicles/page.tsx`

**Step 1: 创建运输类型定义**

```typescript
// apps/admin/src/types/transport.ts
export type VehicleStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'OFFLINE';
export type VehicleType = 'VAN' | 'TRUCK' | 'CONTAINER' | 'REFRIGERATED';
export type DriverStatus = 'IDLE' | 'ON_DUTY' | 'OFF_DUTY' | 'ON_LEAVE';

export const VEHICLE_STATUS_TEXT: Record<VehicleStatus, string> = {
  AVAILABLE: '可用',
  IN_USE: '使用中',
  MAINTENANCE: '维修中',
  OFFLINE: '离线',
};

export const VEHICLE_TYPE_TEXT: Record<VehicleType, string> = {
  VAN: '厢式车',
  TRUCK: '卡车',
  CONTAINER: '集装箱',
  REFRIGERATED: '冷藏车',
};

export const DRIVER_STATUS_TEXT: Record<DriverStatus, string> = {
  IDLE: '空闲',
  ON_DUTY: '出车中',
  OFF_DUTY: '下班',
  ON_LEAVE: '请假',
};

export interface Vehicle {
  id: string;
  plateNumber: string;
  brand: string;
  model: string;
  type: VehicleType;
  capacity: number;
  status: VehicleStatus;
  registrationDate: string;
  nextMaintenanceDate?: string;
  driverId?: string;
  driverName?: string;
}

export interface Driver {
  id: string;
  name: string;
  phone: string;
  licenseNumber: string;
  status: DriverStatus;
  vehicleId?: string;
  vehiclePlateNumber?: string;
}
```

**Step 2: 创建车辆API**

```typescript
// apps/admin/src/features/transport/api/vehicleApi.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { Vehicle, Driver } from '@/types/transport';

export const vehicleApi = createApi({
  reducerPath: 'vehicleApi',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api/v1',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as { auth: { token: string | null } }).auth.token;
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Vehicle', 'Driver'],
  endpoints: (builder) => ({
    getVehicles: builder.query<{ items: Vehicle[]; total: number }, { status?: string; type?: string; pageNum: number; pageSize: number }>({
      query: (params) => ({ url: '/vehicles', params }),
      providesTags: [{ type: 'Vehicle', id: 'LIST' }],
    }),
    getVehicle: builder.query<Vehicle, string>({
      query: (id) => `/vehicles/${id}`,
      providesTags: (_, __, id) => [{ type: 'Vehicle', id }],
    }),
    getDrivers: builder.query<{ items: Driver[]; total: number }, { status?: string; pageNum: number; pageSize: number }>({
      query: (params) => ({ url: '/drivers', params }),
      providesTags: [{ type: 'Driver', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetVehiclesQuery,
  useGetVehicleQuery,
  useGetDriversQuery,
} = vehicleApi;
```

**Step 3: 创建车辆列表组件**

```tsx
// apps/admin/src/features/transport/components/VehicleList/index.tsx
'use client';

import { useState } from 'react';
import { Table, Tag, Card, Typography, Row, Col, Statistic } from 'antd';
import { CarOutlined } from '@ant-design/icons';
import { useGetVehiclesQuery } from '@/features/transport/api/vehicleApi';
import type { Vehicle, VehicleStatus } from '@/types/transport';
import dayjs from 'dayjs';

const { Title } = Typography;

export const VehicleList = () => {
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });
  const { data, isLoading } = useGetVehiclesQuery({
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
  });

  const columns = [
    {
      title: '车牌号',
      dataIndex: 'plateNumber',
      key: 'plateNumber',
      width: 120,
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: '品牌',
      dataIndex: 'brand',
      key: 'brand',
      width: 100,
    },
    {
      title: '车型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, string> = {
          VAN: '厢式车',
          TRUCK: '卡车',
          CONTAINER: '集装箱',
          REFRIGERATED: '冷藏车',
        };
        return typeMap[type] || type;
      },
    },
    {
      title: '载重(kg)',
      dataIndex: 'capacity',
      key: 'capacity',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: VehicleStatus) => {
        const statusMap: Record<string, { color: string; text: string }> = {
          AVAILABLE: { color: 'green', text: '可用' },
          IN_USE: { color: 'blue', text: '使用中' },
          MAINTENANCE: { color: 'orange', text: '维修中' },
          OFFLINE: { color: 'default', text: '离线' },
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '当前司机',
      dataIndex: 'driverName',
      key: 'driverName',
      width: 100,
    },
    {
      title: '登记日期',
      dataIndex: 'registrationDate',
      key: 'registrationDate',
      width: 120,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
  ];

  const stats = {
    total: data?.total || 0,
    available: data?.items?.filter(v => v.status === 'AVAILABLE').length || 0,
    inUse: data?.items?.filter(v => v.status === 'IN_USE').length || 0,
    maintenance: data?.items?.filter(v => v.status === 'MAINTENANCE').length || 0,
  };

  return (
    <Card>
      <div className="page-header">
        <Title level={4} style={{ margin: 0 }}>车辆管理</Title>
      </div>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Statistic title="车辆总数" value={stats.total} prefix={<CarOutlined />} />
        </Col>
        <Col span={6}>
          <Statistic title="可用车辆" value={stats.available} valueStyle={{ color: '#52c41a' }} />
        </Col>
        <Col span={6}>
          <Statistic title="使用中" value={stats.inUse} valueStyle={{ color: '#1890ff' }} />
        </Col>
        <Col span={6}>
          <Statistic title="维修中" value={stats.maintenance} valueStyle={{ color: '#faad14' }} />
        </Col>
      </Row>

      <Table
        columns={columns}
        dataSource={data?.items}
        loading={isLoading}
        rowKey="id"
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: data?.total || 0,
          showSizeChanger: true,
        }}
        onChange={(p) => setPagination(p as any)}
        scroll={{ x: 1000 }}
      />
    </Card>
  );
};
```

**Step 4: 创建车辆管理页面**

```tsx
// apps/admin/src/app/(main)/transport/vehicles/page.tsx
'use client';

import { VehicleList } from '@/features/transport/components/VehicleList';

export default function VehiclesPage() {
  return <VehicleList />;
}
```

**Step 5: Commit**

```bash
git add apps/admin/src/types/transport.ts apps/admin/src/features/transport/ apps/admin/src/app/\(main\)/transport/vehicles/
git commit -m "feat(transport): 实现运输管理模块"
```

---

## Phase 5: 客户管理模块

### Task 11: 实现客户管理模块

**Files:**
- Create: `apps/admin/src/types/customer.ts`
- Create: `apps/admin/src/features/customers/api/customersApi.ts`
- Create: `apps/admin/src/features/customers/components/CustomerList/index.tsx`
- Create: `apps/admin/src/app/(main)/customers/page.tsx`

**Step 1: 创建客户类型定义**

```typescript
// apps/admin/src/types/customer.ts
export type CustomerType = 'ENTERPRISE' | 'INDIVIDUAL' | 'GOVERNMENT';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export const CUSTOMER_TYPE_TEXT: Record<CustomerType, string> = {
  ENTERPRISE: '企业客户',
  INDIVIDUAL: '个人客户',
  GOVERNMENT: '政府客户',
};

export const CUSTOMER_STATUS_TEXT: Record<CustomerStatus, string> = {
  ACTIVE: '正常',
  INACTIVE: '停用',
  SUSPENDED: '已暂停',
};

export interface Customer {
  id: string;
  name: string;
  code: string;
  type: CustomerType;
  contactName: string;
  contactPhone: string;
  contactEmail?: string;
  address: {
    province: string;
    city: string;
    district: string;
    detail: string;
  };
  status: CustomerStatus;
  creditLimit: number;
  createdAt: string;
}
```

**Step 2: 创建客户API**

```typescript
// apps/admin/src/features/customers/api/customersApi.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { Customer } from '@/types/customer';

export const customersApi = createApi({
  reducerPath: 'customersApi',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api/v1/customers',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as { auth: { token: string | null } }).auth.token;
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Customer'],
  endpoints: (builder) => ({
    getCustomers: builder.query<{ items: Customer[]; total: number }, { name?: string; type?: string; status?: string; pageNum: number; pageSize: number }>({
      query: (params) => ({ url: '', params }),
      providesTags: [{ type: 'Customer', id: 'LIST' }],
    }),
    getCustomer: builder.query<Customer, string>({
      query: (id) => `/${id}`,
      providesTags: (_, __, id) => [{ type: 'Customer', id }],
    }),
  }),
});

export const {
  useGetCustomersQuery,
  useGetCustomerQuery,
} = customersApi;
```

**Step 3: 创建客户列表组件**

```tsx
// apps/admin/src/features/customers/components/CustomerList/index.tsx
'use client';

import { useState } from 'react';
import { Table, Tag, Card, Typography, Row, Col, Statistic, Input, Select, Space } from 'antd';
import { UserOutlined, Search } from '@ant-design/icons';
import { useGetCustomersQuery } from '@/features/customers/api/customersApi';
import type { Customer, CustomerType, CustomerStatus } from '@/types/customer';
import { useRouter } from 'next/navigation';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

export const CustomerList = () => {
  const router = useRouter();
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });
  const [filters, setFilters] = useState<Record<string, unknown>>({});

  const { data, isLoading } = useGetCustomersQuery({
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    ...filters,
  } as any);

  const handleSearch = (values: Record<string, unknown>) => {
    setFilters(values);
    setPagination({ ...pagination, current: 1 });
  };

  const columns = [
    {
      title: '客户编码',
      dataIndex: 'code',
      key: 'code',
      width: 140,
      render: (text: string) => <Tag>{text}</Tag>,
    },
    {
      title: '客户名称',
      dataIndex: 'name',
      key: 'name',
      width: 160,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: CustomerType) => {
        const typeMap: Record<CustomerType, string> = {
          ENTERPRISE: '企业',
          INDIVIDUAL: '个人',
          GOVERNMENT: '政府',
        };
        return <Tag>{typeMap[type]}</Tag>;
      },
    },
    {
      title: '联系人',
      key: 'contact',
      width: 120,
      render: (_: unknown, record: Customer) => (
        <Space direction="vertical" size={0}>
          <span>{record.contactName}</span>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.contactPhone}</Text>
        </Space>
      ),
    },
    {
      title: '地址',
      key: 'address',
      render: (_: unknown, record: Customer) =>
        `${record.address.province}${record.address.city}${record.address.detail}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: CustomerStatus) => {
        const statusMap: Record<CustomerStatus, { color: string; text: string }> = {
          ACTIVE: { color: 'green', text: '正常' },
          INACTIVE: { color: 'default', text: '停用' },
          SUSPENDED: { color: 'red', text: '暂停' },
        };
        const config = statusMap[status];
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '信用额度',
      dataIndex: 'creditLimit',
      key: 'creditLimit',
      width: 120,
      render: (limit: number) => `¥${limit?.toLocaleString() || 0}`,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
  ];

  return (
    <Card>
      <div className="page-header">
        <Title level={4} style={{ margin: 0 }}>客户管理</Title>
      </div>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Statistic title="客户总数" value={data?.total || 0} prefix={<UserOutlined />} />
        </Col>
      </Row>

      <Space style={{ marginBottom: 16 }}>
        <Input
          placeholder="客户名称"
          prefix={<Search />}
          style={{ width: 200 }}
          onChange={(e) => handleSearch({ name: e.target.value })}
          allowClear
        />
        <Select
          placeholder="客户类型"
          style={{ width: 120 }}
          allowClear
          options={[
            { value: 'ENTERPRISE', label: '企业客户' },
            { value: 'INDIVIDUAL', label: '个人客户' },
            { value: 'GOVERNMENT', label: '政府客户' },
          ]}
          onChange={(value) => handleSearch({ type: value })}
        />
        <Select
          placeholder="状态"
          style={{ width: 100 }}
          allowClear
          options={[
            { value: 'ACTIVE', label: '正常' },
            { value: 'INACTIVE', label: '停用' },
          ]}
          onChange={(value) => handleSearch({ status: value })}
        />
      </Space>

      <Table
        columns={columns}
        dataSource={data?.items}
        loading={isLoading}
        rowKey="id"
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: data?.total || 0,
          showSizeChanger: true,
        }}
        onChange={(p) => setPagination(p as any)}
        scroll={{ x: 1200 }}
      />
    </Card>
  );
};
```

**Step 4: 创建客户管理页面**

```tsx
// apps/admin/src/app/(main)/customers/page.tsx
'use client';

import { CustomerList } from '@/features/customers/components/CustomerList';

export default function CustomersPage() {
  return <CustomerList />;
}
```

**Step 5: Commit**

```bash
git add apps/admin/src/types/customer.ts apps/admin/src/features/customers/ apps/admin/src/app/\(main\)/customers/
git commit -m "feat(customers): 实现客户管理模块"
```

---

## Phase 6: 仪表盘

### Task 12: 实现仪表盘

**Files:**
- Create: `apps/admin/src/app/(main)/dashboard/page.tsx`

**Step 1: 创建仪表盘页面**

```tsx
// apps/admin/src/app/(main)/dashboard/page.tsx
'use client';

import { Row, Col, Card, Statistic, List, Tag, Typography, Spin } from 'antd';
import { ShoppingCartOutlined, CarOutlined, ArrowUpOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useGetOrdersQuery } from '@/features/orders/api/ordersApi';
import { useGetDispatchesQuery } from '@/features/dispatch/api/dispatchApi';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

export default function DashboardPage() {
  const { data: ordersData, isLoading: ordersLoading } = useGetOrdersQuery({
    pageNum: 1,
    pageSize: 100,
  });
  const { data: dispatchesData, isLoading: dispatchesLoading } = useGetDispatchesQuery({
    pageNum: 1,
    pageSize: 100,
  });

  const loading = ordersLoading || dispatchesLoading;

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  const recentOrders = ordersData?.items?.slice(0, 5) || [];
  const pendingOrders = ordersData?.items?.filter(o => o.status === 'PENDING').length || 0;
  const inTransitOrders = ordersData?.items?.filter(o => o.status === 'IN_TRANSIT').length || 0;
  const todayOrders = ordersData?.items?.filter(o => {
    const orderDate = dayjs(o.createdAt).format('YYYY-MM-DD');
    return orderDate === dayjs().format('YYYY-MM-DD');
  }).length || 0;

  const statusColors: Record<string, string> = {
    PENDING: 'gold',
    DISPATCHED: 'blue',
    IN_TRANSIT: 'cyan',
    DELIVERED: 'green',
    COMPLETED: 'default',
  };

  return (
    <div>
      <Title level={3} style={{ marginBottom: 24 }}>数据概览</Title>

      <Row gutter={[16, 16]}>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日订单"
              value={todayOrders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待调度订单"
              value={pendingOrders}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="运输中"
              value={inTransitOrders}
              prefix={<ArrowUpOutlined />}
              valueStyle={{ color: '#13c2c2' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日调度"
              value={dispatchesData?.items?.length || 0}
              prefix={<CarOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={12}>
          <Card title="最近订单" bordered={false} style={{ height: '100%' }}>
            <List
              dataSource={recentOrders}
              renderItem={(order) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Tag color={statusColors[order.status]}>
                        {order.status === 'PENDING' ? '待调度' :
                         order.status === 'DISPATCHED' ? '已调度' :
                         order.status === 'IN_TRANSIT' ? '运输中' :
                         order.status === 'DELIVERED' ? '已送达' : order.status}
                      </Tag>
                    }
                    description={
                      <Text type="secondary">
                        {order.pickupAddress?.city} → {order.deliveryAddress?.city}
                      </Text>
                    }
                  />
                  <div style={{ textAlign: 'right' }}>
                    <Text strong>¥{order.totalAmount?.toFixed(2)}</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {dayjs(order.createdAt).format('HH:mm')}
                    </Text>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="订单状态分布" bordered={false} style={{ height: '100%' }}>
            <Row gutter={[16, 16]}>
              <Col span={8}>
                <Card size="small" style={{ textAlign: 'center' }}>
                  <Statistic
                    title="待调度"
                    value={ordersData?.items?.filter(o => o.status === 'PENDING').length || 0}
                    valueStyle={{ color: '#faad14', fontSize: 28 }}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ textAlign: 'center' }}>
                  <Statistic
                    title="运输中"
                    value={ordersData?.items?.filter(o => o.status === 'IN_TRANSIT').length || 0}
                    valueStyle={{ color: '#13c2c2', fontSize: 28 }}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ textAlign: 'center' }}>
                  <Statistic
                    title="已完成"
                    value={ordersData?.items?.filter(o => o.status === 'COMPLETED').length || 0}
                    valueStyle={{ color: '#52c41a', fontSize: 28 }}
                  />
                </Card>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
```

**Step 2: Commit**

```bash
git add apps/admin/src/app/\(main\)/dashboard/page.tsx
git commit -m "feat(dashboard): 实现仪表盘页面"
```

---

## Phase 7: 移动端司机端

### Task 13: 初始化移动端司机端

**Files:**
- Create: `apps/driver/package.json`
- Create: `apps/driver/app.json`
- Create: `apps/driver/babel.config.js`
- Create: `apps/driver/src/app/index.tsx`

**Step 1: 创建driver package.json**

```json
{
  "name": "@gh-transport/driver",
  "version": "1.0.0",
  "main": "node_modules/expo/AppEntry.js",
  "scripts": {
    "start": "expo start",
    "android": "expo start --android",
    "ios": "expo start --ios",
    "web": "expo start --web"
  },
  "dependencies": {
    "expo": "~50.0.0",
    "react": "18.2.0",
    "react-dom": "18.2.0",
    "react-native": "0.73.0",
    "expo-router": "~3.4.0",
    "expo-status-bar": "~1.11.0",
    "react-native-safe-area-context": "4.8.0",
    "react-native-screens": "~3.29.0",
    "@react-navigation/native": "^6.1.0",
    "@react-navigation/bottom-tabs": "^6.5.0",
    "zustand": "^4.4.0",
    "axios": "^1.6.0",
    "dayjs": "^1.11.0",
    "@ant-design/icons-react-native": "^2.3.0"
  },
  "devDependencies": {
    "@babel/core": "^7.20.0",
    "@types/react": "~18.2.0",
    "typescript": "^5.1.0"
  },
  "private": true
}
```

**Step 2: 创建app.json**

```json
{
  "expo": {
    "name": "GH司机端",
    "slug": "gh-transport-driver",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "userInterfaceStyle": "light",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#ffffff"
    },
    "assetBundlePatterns": ["**/*"],
    "ios": {
      "supportsTablet": true,
      "bundleIdentifier": "com.ghtransport.driver"
    },
    "android": {
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#ffffff"
      },
      "package": "com.ghtransport.driver"
    },
    "web": {
      "favicon": "./assets/favicon.png"
    }
  }
}
```

**Step 3: 创建根页面**

```tsx
// apps/driver/src/app/index.tsx
import { Redirect } from 'expo-router';

export default function App() {
  return <Redirect href="/login" />;
}
```

**Step 4: Commit**

```bash
git add apps/driver/
git commit -m "feat(driver): 初始化React Native司机端"
```

---

### Task 14: 实现司机端登录页

**Files:**
- Create: `apps/driver/src/app/login.tsx`
- Create: `apps/driver/src/features/auth/store/useAuthStore.ts`

**Step 1: 创建Auth Store**

```typescript
// apps/driver/src/features/auth/store/useAuthStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: string;
  name: string;
  phone: string;
  avatar?: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (user: User, token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (user, token) => set({ user, token, isAuthenticated: true }),
      logout: () => set({ user: null, token: null, isAuthenticated: false }),
    }),
    {
      name: 'driver-auth',
    }
  )
);
```

**Step 2: 创建登录页面**

```tsx
// apps/driver/src/app/login.tsx
import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuthStore } from '@/features/auth/store/useAuthStore';

export default function LoginScreen() {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const login = useAuthStore((state) => state.login);

  const handleLogin = async () => {
    if (!phone || !password) {
      Alert.alert('提示', '请输入手机号和密码');
      return;
    }

    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      login({ id: '1', name: '张司机', phone }, 'mock-token-xxx');
      router.replace('/(main)/home');
    } catch (error) {
      Alert.alert('登录失败', '请检查手机号和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={styles.content}>
        <View style={styles.logoContainer}>
          <Text style={styles.logoText}>GH Transport</Text>
          <Text style={styles.subtitle}>司机端</Text>
        </View>

        <View style={styles.form}>
          <Text style={styles.label}>手机号</Text>
          <TextInput
            style={styles.input}
            placeholder="请输入手机号"
            value={phone}
            onChangeText={setPhone}
            keyboardType="phone-pad"
          />

          <Text style={styles.label}>密码</Text>
          <TextInput
            style={styles.input}
            placeholder="请输入密码"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />

          <TouchableOpacity
            style={styles.button}
            onPress={handleLogin}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.buttonText}>登录</Text>
            )}
          </TouchableOpacity>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1890ff',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 32,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 48,
  },
  logoText: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255,255,255,0.8)',
  },
  form: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 24,
  },
  label: {
    fontSize: 14,
    color: '#333',
    marginBottom: 8,
    fontWeight: '500',
  },
  input: {
    height: 48,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    paddingHorizontal: 12,
    marginBottom: 16,
    fontSize: 16,
  },
  button: {
    height: 48,
    backgroundColor: '#1890ff',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
```

**Step 3: Commit**

```bash
git add apps/driver/src/app/login.tsx apps/driver/src/features/auth/
git commit -m "feat(driver): 实现司机端登录功能"
```

---

### Task 15: 实现司机端首页

**Files:**
- Create: `apps/driver/src/app/(main)/_layout.tsx`
- Create: `apps/driver/src/app/(main)/home/index.tsx`

**Step 1: 创建Tab布局**

```tsx
// apps/driver/src/app/(main)/_layout.tsx
import { Tabs } from 'expo-router';
import { Home, List, Scan, User } from '@ant-design/icons-react-native';
import { Platform } from 'react-native';

export default function MainLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          height: Platform.OS === 'ios' ? 88 : 64,
          paddingBottom: Platform.OS === 'ios' ? 28 : 8,
          backgroundColor: '#fff',
          borderTopWidth: 1,
          borderTopColor: '#f0f0f0',
        },
        tabBarActiveTintColor: '#1890ff',
        tabBarInactiveTintColor: '#999',
        tabBarLabelStyle: {
          fontSize: 12,
        },
      }}
    >
      <Tabs.Screen
        name="home/index"
        options={{
          title: '首页',
          tabBarIcon: ({ color }) => <Home name="home" color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="tasks/index"
        options={{
          title: '任务',
          tabBarIcon: ({ color }) => <List name="bars" color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="scan/index"
        options={{
          title: '扫码',
          tabBarIcon: ({ color }) => <Scan name="scan" color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="profile/index"
        options={{
          title: '我的',
          tabBarIcon: ({ color }) => <User name="user" color={color} size={24} />,
        }}
      />
    </Tabs>
  );
}
```

**Step 2: 创建首页**

```tsx
// apps/driver/src/app/(main)/home/index.tsx
import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, RefreshControl } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import dayjs from 'dayjs';

interface Task {
  id: string;
  orderNo: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';
  pickupAddress: string;
  deliveryAddress: string;
  pickupTime: string;
  deliveryTime: string;
}

export default function HomeScreen() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [refreshing, setRefreshing] = useState(false);
  const [tasks, setTasks] = useState<Task[]>([]);

  const onRefresh = async () => {
    setRefreshing(true);
    await new Promise(resolve => setTimeout(resolve, 1000));
    setTasks([
      {
        id: '1',
        orderNo: 'ORD20250127001',
        status: 'IN_PROGRESS',
        pickupAddress: '深圳市南山区科技园',
        deliveryAddress: '广州市天河区体育西路',
        pickupTime: '2025-01-27 08:00',
        deliveryTime: '2025-01-27 12:00',
      },
      {
        id: '2',
        orderNo: 'ORD20250127002',
        status: 'PENDING',
        pickupAddress: '深圳市福田区华强北',
        deliveryAddress: '深圳市罗湖区火车站',
        pickupTime: '2025-01-27 14:00',
        deliveryTime: '2025-01-27 16:00',
      },
    ]);
    setRefreshing(false);
  };

  useEffect(() => {
    onRefresh();
  }, []);

  const statusColors: Record<string, string> = {
    PENDING: '#faad14',
    IN_PROGRESS: '#1890ff',
    COMPLETED: '#52c41a',
  };

  const statusText: Record<string, string> = {
    PENDING: '待取件',
    IN_PROGRESS: '取件中',
    COMPLETED: '已完成',
  };

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>您好，{user?.name || '司机'}</Text>
          <Text style={styles.date}>{dayjs().format('YYYY年MM月DD日 dddd')}</Text>
        </View>
      </View>

      <View style={styles.statsRow}>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>2</Text>
          <Text style={styles.statLabel}>今日任务</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: '#52c41a' }]}>
          <Text style={[styles.statValue, { color: '#fff' }]}>0</Text>
          <Text style={[styles.statLabel, { color: 'rgba(255,255,255,0.8)' }]}>已完成</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: '#1890ff' }]}>
          <Text style={[styles.statValue, { color: '#fff' }]}>1</Text>
          <Text style={[styles.statLabel, { color: 'rgba(255,255,255,0.8)' }]}>进行中</Text>
        </View>
      </View>

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>待执行任务</Text>
          <TouchableOpacity onPress={() => router.push('/(main)/tasks/index')}>
            <Text style={styles.moreLink}>查看全部</Text>
          </TouchableOpacity>
        </View>

        {tasks.map(task => (
          <TouchableOpacity
            key={task.id}
            style={styles.taskCard}
            onPress={() => router.push(`/(main)/tasks/${task.id}`)}
          >
            <View style={styles.taskHeader}>
              <Text style={styles.orderNo}>{task.orderNo}</Text>
              <View style={[styles.statusBadge, { backgroundColor: statusColors[task.status] }]}>
                <Text style={styles.statusText}>{statusText[task.status]}</Text>
              </View>
            </View>
            <View style={styles.addressRow}>
              <View style={styles.addressPoint}>
                <View style={[styles.dot, { backgroundColor: '#52c41a' }]} />
                <Text style={styles.addressText} numberOfLines={1}>{task.pickupAddress}</Text>
              </View>
              <View style={styles.addressPoint}>
                <View style={[styles.dot, { backgroundColor: '#1890ff' }]} />
                <Text style={styles.addressText} numberOfLines={1}>{task.deliveryAddress}</Text>
              </View>
            </View>
          </TouchableOpacity>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    padding: 16,
    backgroundColor: '#1890ff',
    paddingTop: 60,
  },
  greeting: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  date: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.8)',
    marginTop: 4,
  },
  statsRow: {
    flexDirection: 'row',
    padding: 16,
    gap: 12,
  },
  statCard: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  statValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1890ff',
  },
  statLabel: {
    fontSize: 12,
    color: '#999',
    marginTop: 4,
  },
  section: {
    padding: 16,
    paddingTop: 0,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  moreLink: {
    fontSize: 14,
    color: '#1890ff',
  },
  taskCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  taskHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  orderNo: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '500',
  },
  addressRow: {
    gap: 8,
    marginBottom: 12,
  },
  addressPoint: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  addressText: {
    flex: 1,
    fontSize: 14,
    color: '#666',
  },
});
```

**Step 3: Commit**

```bash
git add apps/driver/src/app/\(main\)/ apps/driver/src/features/tasks/
git commit -m "feat(driver): 实现司机端首页"
```

---

## Phase 8: 测试与优化

### Task 16: 添加单元测试

**Files:**
- Create: `apps/admin/src/__tests__/orders.test.tsx`
- Create: `apps/admin/vitest.config.ts`

**Step 1: 创建订单组件测试**

```typescript
// apps/admin/src/__tests__/orders.test.tsx
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { store } from '@/store';
import { OrderStatusTag } from '@/features/orders/components/OrderStatusTag';

describe('Order Components', () => {
  test('OrderStatusTag renders correct status', () => {
    render(<OrderStatusTag status="PENDING" />);
    expect(screen.getByText('待调度')).toBeTruthy();
  });
});
```

**Step 2: 创建Vitest配置**

```typescript
// apps/admin/vitest.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/setupTests.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

**Step 3: Commit**

```bash
git add apps/admin/src/__tests__/ apps/admin/vitest.config.ts
git commit -m "test: 添加单元测试配置"
```

---

## Phase 9: 核心功能补充（Review后新增）

### Task 17: 实现API拦截器和路由守卫

**Files:**
- Create: `apps/admin/src/middleware.ts`
- Create: `apps/admin/src/services/apiClient.ts`
- Modify: `apps/admin/src/features/auth/store/authSlice.ts`

**Step 1: 创建API客户端拦截器**

```typescript
// apps/admin/src/services/apiClient.ts
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 添加Token
apiClient.interceptors.request.use(
  (config) => {
    const token = typeof window !== 'undefined'
      ? localStorage.getItem('token')
      : null;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器 - 统一错误处理和Token刷新
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 401未授权 - 尝试刷新Token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/api/v1/auth/refresh', {
          refreshToken,
        });
        const { token } = response.data;
        localStorage.setItem('token', token);
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // 刷新失败，清除Token并跳转到登录页
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // 其他错误 - 统一错误提示
    const message = error.response?.data?.message || error.message || '请求失败';
    if (typeof window !== 'undefined') {
      const { message: antdMessage } = await import('antd');
      antdMessage.error(message);
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

**Step 2: 创建路由守卫中间件**

```typescript
// apps/admin/src/middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 需要登录的路径
const protectedPaths = ['/dashboard', '/orders', '/dispatch', '/transport', '/customers', '/settings'];

// 不需要登录的路径
const publicPaths = ['/login', '/api/v1/auth/login'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const token = request.cookies.get('auth-token')?.value;

  // 检查是否为受保护路径
  const isProtectedPath = protectedPaths.some(path => pathname.startsWith(path));

  // 检查是否为公开路径
  const isPublicPath = publicPaths.some(path => pathname.startsWith(path));

  // 未登录访问受保护页面 - 跳转登录
  if (isProtectedPath && !token) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // 已登录访问登录页 - 跳转首页
  if (pathname === '/login' && token) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * 排除以下路径:
     * - api (API路由)
     * - _next/static (静态文件)
     * - _next/image (图片优化)
     * - favicon.ico (网站图标)
     * - public 文件夹
     */
    '/((?!api|_next/static|_next/image|favicon.ico|public).*)',
  ],
};
```

**Step 3: 更新Auth Slice支持持久化**

```typescript
// apps/admin/src/features/auth/store/authSlice.ts
// 在 setCredentials 中添加
localStorage.setItem('token', action.payload.token);
localStorage.setItem('refreshToken', action.payload.refreshToken);

// 在 logout 中添加
localStorage.removeItem('token');
localStorage.removeItem('refreshToken');
```

**Step 4: Commit**

```bash
git add apps/admin/src/middleware.ts apps/admin/src/services/
git commit -m "feat: 添加API拦截器和路由守卫"
```

---

### Task 18: 集成真实API到移动端

**Files:**
- Create: `apps/driver/src/services/api.ts`
- Create: `apps/driver/src/features/tasks/api/tasksApi.ts`
- Create: `apps/driver/src/types/index.ts`
- Modify: `apps/driver/src/app/login.tsx`
- Modify: `apps/driver/src/app/(main)/home/index.tsx`

**Step 1: 创建移动端类型定义**

```typescript
// apps/driver/src/types/index.ts
export interface Task {
  id: string;
  orderNo: string;
  status: 'PENDING' | 'PICKED_UP' | 'IN_TRANSIT' | 'DELIVERED' | 'COMPLETED' | 'EXCEPTION';
  pickupAddress: {
    province: string;
    city: string;
    district: string;
    detail: string;
    contactName: string;
    contactPhone: string;
  };
  deliveryAddress: {
    province: string;
    city: string;
    district: string;
    detail: string;
    contactName: string;
    contactPhone: string;
  };
  pickupTime: string;
  deliveryTime: string;
  items: Array<{
    itemName: string;
    quantity: number;
  }>;
  totalAmount: number;
}

export interface TaskDetail extends Task {
  createdAt: string;
  updatedAt: string;
  driver?: {
    id: string;
    name: string;
    phone: string;
  };
}
```

**Step 2: 创建API服务**

```typescript
// apps/driver/src/services/api.ts
import axios from 'axios';

const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = globalThis.__driverToken__;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 跳转到登录页
      import('expo-router').then(({ router }) => {
        router.replace('/login');
      });
    }
    return Promise.reject(error);
  }
);

export default api;
```

**Step 3: 创建任务API**

```typescript
// apps/driver/src/features/tasks/api/tasksApi.ts
import api from '@/services/api';
import type { Task, TaskDetail } from '@/types';

export const tasksApi = {
  // 获取任务列表
  getTasks: async (params: { status?: string; pageNum: number; pageSize: number }) => {
    const response = await api.get<{ items: Task[]; total: number }>('/driver/tasks', { params });
    return response.data;
  },

  // 获取任务详情
  getTaskDetail: async (taskId: string) => {
    const response = await api.get<TaskDetail>(`/driver/tasks/${taskId}`);
    return response.data;
  },

  // 开始取件
  startPickup: async (taskId: string) => {
    const response = await api.post(`/driver/tasks/${taskId}/pickup`);
    return response.data;
  },

  // 确认送达
  confirmDelivery: async (taskId: string, signature?: string, photos?: string[]) => {
    const response = await api.post(`/driver/tasks/${taskId}/deliver`, { signature, photos });
    return response.data;
  },

  // 上传签收凭证
  uploadProof: async (taskId: string, formData: FormData) => {
    const response = await api.post(`/driver/tasks/${taskId}/proof`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};
```

**Step 4: 更新登录页集成真实API**

```typescript
// apps/driver/src/app/login.tsx
const handleLogin = async () => {
  if (!phone || !password) {
    Alert.alert('提示', '请输入手机号和密码');
    return;
  }

  setLoading(true);
  try {
    const response = await api.post('/auth/driver/login', {
      phone,
      password,
    });
    const { token, user } = response.data;
    globalThis.__driverToken__ = token;
    login(user, token);
    router.replace('/(main)/home');
  } catch (error: any) {
    const message = error.response?.data?.message || '登录失败，请检查手机号和密码';
    Alert.alert('登录失败', message);
  } finally {
    setLoading(false);
  }
};
```

**Step 5: 更新首页集成真实API**

```typescript
// apps/driver/src/app/(main)/home/index.tsx
const fetchTasks = async () => {
  const response = await tasksApi.getTasks({
    pageNum: 1,
    pageSize: 100,
  });
  setTasks(response.items);
};

// 修改 onRefresh 使用真实API
const onRefresh = async () => {
  setRefreshing(true);
  try {
    await fetchTasks();
  } catch (error) {
    Alert.alert('错误', '获取任务列表失败');
  } finally {
    setRefreshing(false);
  }
};

useEffect(() => {
  fetchTasks();
}, []);
```

**Step 6: Commit**

```bash
git add apps/driver/src/services/ apps/driver/src/features/tasks/ apps/driver/src/types/
git commit -m "feat(driver): 集成真实API到移动端"
```

---

### Task 19: 实现移动端任务详情页

**Files:**
- Create: `apps/driver/src/app/(main)/tasks/[id].tsx`
- Create: `apps/driver/src/features/tasks/components/TaskDetailCard/index.tsx`

**Step 1: 创建任务详情卡片组件**

```typescript
// apps/driver/src/features/tasks/components/TaskDetailCard/index.tsx
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useState } from 'react';
import { useRouter } from 'expo-router';
import { tasksApi } from '@/features/tasks/api/tasksApi';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import dayjs from 'dayjs';

interface TaskDetailCardProps {
  task: any;
  onRefresh: () => void;
}

export const TaskDetailCard = ({ task, onRefresh }: TaskDetailCardProps) => {
  const router = useRouter();
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);

  const statusConfig: Record<string, { color: string; text: string; action?: string }> = {
    PENDING: { color: '#faad14', text: '待取件', action: '开始取件' },
    PICKED_UP: { color: '#1890ff', text: '已取件', action: '开始配送' },
    IN_TRANSIT: { color: '#13c2c2', text: '配送中', action: '确认送达' },
    DELIVERED: { color: '#52c41a', text: '已送达' },
    COMPLETED: { color: '#722ed1', text: '已完成' },
    EXCEPTION: { color: '#ff4d4f', text: '异常' },
  };

  const config = statusConfig[task.status] || { color: '#999', text: task.status };

  const handleAction = async () => {
    setLoading(true);
    try {
      if (task.status === 'PENDING') {
        await tasksApi.startPickup(task.id);
        Alert.alert('成功', '已开始取件');
      } else if (task.status === 'IN_TRANSIT') {
        router.push(`/(main)/tasks/${task.id}/confirm`);
      }
      onRefresh();
    } catch (error) {
      Alert.alert('错误', '操作失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.orderNo}>{task.orderNo}</Text>
        <View style={[styles.statusBadge, { backgroundColor: config.color }]}>
          <Text style={styles.statusText}>{config.text}</Text>
        </View>
      </View>

      <View style={styles.section}>
        <View style={styles.addressCard}>
          <View style={styles.addressRow}>
            <View style={styles.dotWrapper}>
              <View style={[styles.dot, { backgroundColor: '#52c41a' }]} />
              <Text style={styles.dotLabel}>取</Text>
            </View>
            <View style={styles.addressInfo}>
              <Text style={styles.addressLabel}>取件地址</Text>
              <Text style={styles.addressText}>
                {task.pickupAddress.province}{task.pickupAddress.city}{task.pickupAddress.detail}
              </Text>
              <Text style={styles.contactText}>
                {task.pickupAddress.contactName} {task.pickupAddress.contactPhone}
              </Text>
            </View>
          </View>

          <View style={styles.divider} />

          <View style={styles.addressRow}>
            <View style={styles.dotWrapper}>
              <View style={[styles.dot, { backgroundColor: '#1890ff' }]} />
              <Text style={[styles.dotLabel, { color: '#1890ff' }]}>送</Text>
            </View>
            <View style={styles.addressInfo}>
              <Text style={styles.addressLabel}>送件地址</Text>
              <Text style={styles.addressText}>
                {task.deliveryAddress.province}{task.deliveryAddress.city}{task.deliveryAddress.detail}
              </Text>
              <Text style={styles.contactText}>
                {task.deliveryAddress.contactName} {task.deliveryAddress.contactPhone}
              </Text>
            </View>
          </View>
        </View>
      </View>

      <View style={styles.section}>
        <View style={styles.infoCard}>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>取件时间</Text>
            <Text style={styles.infoValue}>{dayjs(task.pickupTime).format('YYYY-MM-DD HH:mm')}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>送达时间</Text>
            <Text style={styles.infoValue}>{dayjs(task.deliveryTime).format('YYYY-MM-DD HH:mm')}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>货物件数</Text>
            <Text style={styles.infoValue}>{task.items?.length || 0} 件</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>备注</Text>
            <Text style={styles.infoValue}>{task.remark || '无'}</Text>
          </View>
        </View>
      </View>

      {config.action && (
        <TouchableOpacity
          style={styles.actionButton}
          onPress={handleAction}
          disabled={loading}
        >
          <Text style={styles.actionButtonText}>
            {loading ? '处理中...' : config.action}
          </Text>
        </TouchableOpacity>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#fff',
  },
  orderNo: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '500',
  },
  section: {
    padding: 16,
  },
  addressCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
  },
  addressRow: {
    flexDirection: 'row',
    gap: 12,
  },
  dotWrapper: {
    alignItems: 'center',
  },
  dot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  dotLabel: {
    fontSize: 10,
    color: '#52c41a',
    marginTop: 2,
  },
  addressInfo: {
    flex: 1,
  },
  addressLabel: {
    fontSize: 12,
    color: '#999',
    marginBottom: 4,
  },
  addressText: {
    fontSize: 14,
    color: '#333',
    marginBottom: 4,
  },
  contactText: {
    fontSize: 12,
    color: '#666',
  },
  divider: {
    height: 1,
    backgroundColor: '#f0f0f0',
    marginVertical: 12,
    marginLeft: 18,
  },
  infoCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
  },
  infoLabel: {
    fontSize: 14,
    color: '#999',
  },
  infoValue: {
    fontSize: 14,
    color: '#333',
  },
  actionButton: {
    margin: 16,
    height: 48,
    backgroundColor: '#1890ff',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  actionButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
```

**Step 2: 创建任务详情页面**

```typescript
// apps/driver/src/app/(main)/tasks/[id].tsx
import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, ScrollView, RefreshControl } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { TaskDetailCard } from '@/features/tasks/components/TaskDetailCard';
import { tasksApi } from '@/features/tasks/api/tasksApi';
import type { TaskDetail } from '@/types';

export default function TaskDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [task, setTask] = useState<TaskDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchTask = async () => {
    try {
      const data = await tasksApi.getTaskDetail(id);
      setTask(data);
    } catch (error) {
      console.error('获取任务详情失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await fetchTask();
    setRefreshing(false);
  };

  useEffect(() => {
    fetchTask();
  }, [id]);

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#1890ff" />
      </View>
    );
  }

  if (!task) {
    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyText}>任务不存在</Text>
      </View>
    );
  }

  return <TaskDetailCard task={task} onRefresh={fetchTask} />;
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
  },
});
```

**Step 3: Commit**

```bash
git add apps/driver/src/app/\(main\)/tasks/ apps/driver/src/features/tasks/components/
git commit -m "feat(driver): 实现移动端任务详情页"
```

---

### Task 20: 实现移动端扫码功能

**Files:**
- Create: `apps/driver/src/app/(main)/scan/index.tsx`
- Create: `apps/driver/src/features/scan/hooks/useScanner.ts`

**Step 1: 创建扫码Hook**

```typescript
// apps/driver/src/features/scan/hooks/useScanner.ts
import { useState, useEffect } from 'react';
import { Platform } from 'react-native';
import * as ExpoCamera from 'expo-camera';

export function useScanner() {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [scanned, setScanned] = useState(false);
  const [scanResult, setScanResult] = useState<string | null>(null);

  useEffect(() => {
    const getPermission = async () => {
      if (Platform.OS === 'web') {
        setHasPermission(true);
        return;
      }
      const { status } = await ExpoCamera.requestCameraPermissionsAsync();
      setHasPermission(status === 'granted');
    };
    getPermission();
  }, []);

  const handleBarCodeScanned = ({ data }: { data: string }) => {
    setScanned(true);
    setScanResult(data);
  };

  const resetScanner = () => {
    setScanned(false);
    setScanResult(null);
  };

  return {
    hasPermission,
    scanned,
    scanResult,
    handleBarCodeScanned,
    resetScanner,
  };
}
```

**Step 2: 创建扫码页面**

```typescript
// apps/driver/src/app/(main)/scan/index.tsx
import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal, Alert, Button } from 'react-native';
import { useRouter } from 'expo-router';
import { useScanner } from '@/features/scan/hooks/useScanner';
import { tasksApi } from '@/features/tasks/api/tasksApi';

export default function ScanScreen() {
  const router = useRouter();
  const { hasPermission, scanned, scanResult, handleBarCodeScanned, resetScanner } = useScanner();
  const [showResult, setShowResult] = useState(false);

  const handleOrderCode = async (code: string) => {
    try {
      // 解析二维码内容 (格式: ORDER:{orderId} 或 原始orderId)
      const orderId = code.startsWith('ORDER:') ? code.replace('ORDER:', '') : code;
      const task = await tasksApi.getTaskDetail(orderId);
      router.push(`/(main)/tasks/${task.id}`);
    } catch (error) {
      Alert.alert('提示', '未找到对应任务，是否创建取件单？', [
        { text: '取消', style: 'cancel' },
        { text: '创建', onPress: () => router.push('/(main)/tasks/create') },
      ]);
    }
  };

  // Web端扫码模拟
  const handleWebScan = () => {
    const mockCode = `ORDER:${Date.now()}`;
    handleBarCodeScanned({ data: mockCode });
  };

  if (Platform.OS === 'web') {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>扫码功能</Text>
        <View style={styles.webPreview}>
          <Text style={styles.webText}>摄像头预览区域</Text>
          <Button title="模拟扫码" onPress={handleWebScan} />
        </View>
        {scanned && (
          <View style={styles.resultContainer}>
            <Text style={styles.resultText}>扫描结果: {scanResult}</Text>
            <View style={styles.resultActions}>
              <TouchableOpacity
                style={styles.resultButton}
                onPress={() => handleOrderCode(scanResult!)}
              >
                <Text style={styles.resultButtonText}>查看任务</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.resultButton} onPress={resetScanner}>
                <Text style={styles.resultButtonText}>重新扫描</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>扫码功能</Text>
      {hasPermission === null ? (
        <Text style={styles.info}>正在请求相机权限...</Text>
      ) : hasPermission === false ? (
        <Text style={styles.error}>没有相机权限，请在设置中开启</Text>
      ) : (
        <View style={styles.cameraContainer}>
          <ExpoCamera.Camera
            style={StyleSheet.absoluteFillObject}
            onBarCodeScanned={scanned ? undefined : handleBarCodeScanned}
            barCodeScannerSettings={{
              barCodeTypes: ['qr', 'ean13', 'ean8', 'code128', 'code39', 'code93', 'upc_a', 'upc_e'],
            }}
          />
          <View style={styles.scanOverlay}>
            <View style={styles.scanFrame} />
          </View>
          {scanned && (
            <View style={styles.resultContainer}>
              <Text style={styles.resultText}>扫描结果: {scanResult}</Text>
              <View style={styles.resultActions}>
                <TouchableOpacity
                  style={styles.resultButton}
                  onPress={() => handleOrderCode(scanResult!)}
                >
                  <Text style={styles.resultButtonText}>查看任务</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.resultButton} onPress={resetScanner}>
                  <Text style={styles.resultButtonText}>重新扫描</Text>
                </TouchableOpacity>
              </View>
            </View>
          )}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
    alignItems: 'center',
    paddingTop: 60,
  },
  title: {
    color: '#fff',
    fontSize: 18,
    marginBottom: 20,
  },
  info: {
    color: '#fff',
    fontSize: 14,
  },
  error: {
    color: '#ff4d4f',
    fontSize: 14,
  },
  webPreview: {
    width: 300,
    height: 300,
    backgroundColor: '#333',
    justifyContent: 'center',
    alignItems: 'center',
  },
  webText: {
    color: '#fff',
    marginBottom: 16,
  },
  cameraContainer: {
    width: '100%',
    height: '100%',
  },
  scanOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.5)',
  },
  scanFrame: {
    width: 250,
    height: 250,
    borderWidth: 2,
    borderColor: '#1890ff',
    borderRadius: 12,
  },
  resultContainer: {
    position: 'absolute',
    bottom: 100,
    left: 20,
    right: 20,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
  },
  resultText: {
    fontSize: 14,
    color: '#333',
    marginBottom: 16,
    textAlign: 'center',
  },
  resultActions: {
    flexDirection: 'row',
    gap: 12,
  },
  resultButton: {
    flex: 1,
    height: 44,
    backgroundColor: '#1890ff',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  resultButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
});
```

**Step 3: Commit**

```bash
git add apps/driver/src/app/\(main\)/scan/ apps/driver/src/features/scan/
git commit -m "feat(driver): 实现移动端扫码功能"
```

---

### Task 21: 实现移动端个人中心页

**Files:**
- Create: `apps/driver/src/app/(main)/profile/index.tsx`
- Create: `apps/driver/src/features/profile/components/StatCard/index.tsx`

**Step 1: 创建个人中心页面**

```typescript
// apps/driver/src/app/(main)/profile/index.tsx
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import dayjs from 'dayjs';

interface ProfileItem {
  icon: string;
  title: string;
  onPress: () => void;
}

export default function ProfileScreen() {
  const router = useRouter();
  const { user, logout } = useAuthStore();

  const handleLogout = () => {
    Alert.alert('确认', '确定要退出登录吗？', [
      { text: '取消', style: 'cancel' },
      {
        text: '退出',
        style: 'destructive',
        onPress: () => {
          logout();
          router.replace('/login');
        },
      },
    ]);
  };

  const stats = [
    { label: '今日完成', value: 5 },
    { label: '本月完成', value: 128 },
    { label: '累计里程', value: '3650km' },
  ];

  const menuItems: ProfileItem[] = [
    { icon: 'car', title: '我的车辆', onPress: () => router.push('/(main)/profile/vehicle') },
    { icon: 'file-text', title: '我的订单', onPress: () => router.push('/(main)/tasks') },
    { icon: 'history', title: '工作记录', onPress: () => router.push('/(main)/profile/history') },
    { icon: 'setting', title: '账户设置', onPress: () => router.push('/(main)/profile/settings') },
    { icon: 'question-circle', title: '帮助中心', onPress: () => router.push('/(main)/profile/help') },
  ];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <View style={styles.avatarWrapper}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{user?.name?.charAt(0) || '司'}</Text>
          </View>
        </View>
        <Text style={styles.name}>{user?.name || '司机'}</Text>
        <Text style={styles.phone}>{user?.phone || '未设置手机号'}</Text>
      </View>

      <View style={styles.statsContainer}>
        {stats.map((stat, index) => (
          <View key={index} style={styles.statItem}>
            <Text style={styles.statValue}>{stat.value}</Text>
            <Text style={styles.statLabel}>{stat.label}</Text>
          </View>
        ))}
      </View>

      <View style={styles.menuContainer}>
        {menuItems.map((item, index) => (
          <TouchableOpacity
            key={index}
            style={styles.menuItem}
            onPress={item.onPress}
          >
            <View style={styles.menuLeft}>
              <Text style={styles.menuIcon}>{item.icon}</Text>
              <Text style={styles.menuTitle}>{item.title}</Text>
            </View>
            <Text style={styles.menuArrow}>›</Text>
          </TouchableOpacity>
        ))}
      </View>

      <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
        <Text style={styles.logoutText}>退出登录</Text>
      </TouchableOpacity>

      <View style={styles.version}>
        <Text style={styles.versionText}>GH司机端 v1.0.0</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#1890ff',
    paddingTop: 60,
    paddingBottom: 40,
    alignItems: 'center',
  },
  avatarWrapper: {
    marginBottom: 12,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarText: {
    fontSize: 32,
    color: '#1890ff',
    fontWeight: '600',
  },
  name: {
    fontSize: 20,
    color: '#fff',
    fontWeight: '600',
    marginBottom: 4,
  },
  phone: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.8)',
  },
  statsContainer: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    marginHorizontal: 16,
    marginTop: -30,
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statItem: {
    flex: 1,
    alignItems: 'center',
  },
  statValue: {
    fontSize: 20,
    fontWeight: '600',
    color: '#1890ff',
  },
  statLabel: {
    fontSize: 12,
    color: '#999',
    marginTop: 4,
  },
  menuContainer: {
    backgroundColor: '#fff',
    marginTop: 16,
    paddingHorizontal: 16,
  },
  menuItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  menuLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  menuIcon: {
    fontSize: 18,
    color: '#1890ff',
  },
  menuTitle: {
    fontSize: 16,
    color: '#333',
  },
  menuArrow: {
    fontSize: 18,
    color: '#ccc',
  },
  logoutButton: {
    margin: 16,
    height: 48,
    backgroundColor: '#fff',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#ff4d4f',
  },
  logoutText: {
    color: '#ff4d4f',
    fontSize: 16,
    fontWeight: '500',
  },
  version: {
    alignItems: 'center',
    paddingBottom: 32,
  },
  versionText: {
    fontSize: 12,
    color: '#999',
  },
});
```

**Step 2: Commit**

```bash
git add apps/driver/src/app/\(main\)/profile/ apps/driver/src/features/profile/
git commit -m "feat(driver): 实现移动端个人中心页"
```

---

### Task 22: 添加错误边界和全局错误处理

**Files:**
- Create: `apps/admin/src/components/ErrorBoundary.tsx`
- Create: `apps/admin/src/components/GlobalError.tsx`
- Modify: `apps/admin/src/app/(main)/layout.tsx`

**Step 1: 创建错误边界组件**

```typescript
// apps/admin/src/components/ErrorBoundary.tsx
'use client';

import { Component, ErrorInfo, ReactNode } from 'react';
import { Result, Button } from 'antd';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  private handleRetry = () => {
    this.setState({ hasError: false, error: undefined });
  };

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }
      return (
        <Result
          status="error"
          title="页面发生错误"
          subTitle={this.state.error?.message || '未知错误'}
          extra={[
            <Button type="primary" key="retry" onClick={this.handleRetry}>
              重试
            </Button>,
            <Button key="reload" onClick={() => window.location.reload()}>
              刷新页面
            </Button>,
          ]}
        />
      );
    }
    return this.props.children;
  }
}
```

**Step 2: 创建全局错误处理组件**

```typescript
// apps/admin/src/components/GlobalError.tsx
'use client';

import { useEffect } from 'react';

export function GlobalError({ error, reset }: { error: Error; reset: () => void }) {
  useEffect(() => {
    console.error('Global error:', error);
  }, [error]);

  return (
    <html>
      <body style={{ margin: 0, padding: 20 }}>
        <h1>应用程序发生错误</h1>
        <p>{error.message}</p>
        <button onClick={() => reset()}>重试</button>
      </body>
    </html>
  );
}
```

**Step 3: 在主布局中使用错误边界**

```tsx
// apps/admin/src/app/(main)/layout.tsx
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { MainLayout } from '@/components/layout/MainLayout';

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <MainLayout>
      <ErrorBoundary
        fallback={
          <Result
            status="warning"
            title="加载失败"
            subTitle="页面内容加载失败，请刷新重试"
            extra={<Button onClick={() => window.location.reload()}>刷新</Button>}
          />
        }
      >
        {children}
      </ErrorBoundary>
    </MainLayout>
  );
}
```

**Step 4: Commit**

```bash
git add apps/admin/src/components/ErrorBoundary.tsx apps/admin/src/app/\(main\)/layout.tsx
git commit -m "feat: 添加错误边界和全局错误处理"
```

---

### Task 23: 配置环境变量和Git Hooks

**Files:**
- Create: `apps/admin/.env.local.example`
- Create: `apps/driver/.env.example`
- Create: `.env.example`
- Create: `.husky/pre-commit`
- Create: `.husky/commit-msg`
- Create: `commitlint.config.js`
- Modify: `package.json`

**Step 1: 创建环境变量模板**

```bash
# apps/admin/.env.local.example
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=GH Transport
```

```bash
# apps/driver/.env.example
EXPO_PUBLIC_API_URL=http://localhost:8080/api/v1
```

```bash
# .env.example
# Monorepo环境变量模板
# 复制此文件为 .env.local 并填写真实值
```

**Step 2: 配置Git Hooks**

```bash
# 安装husky
npm install -D husky

# 初始化husky
npx husky install

# 添加pre-commit hook
npx husky add .husky/pre-commit 'npm run lint'

# 添加commit-msg hook
npx husky add .husky/commit-msg 'npx commitlint --edit "$1"'
```

**Step 3: 创建commitlint配置**

```javascript
// commitlint.config.js
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'docs', 'style', 'refactor', 'test', 'chore', 'perf'],
    ],
    'subject-case': [0, 'always', 'sentence-case'],
  },
};
```

**Step 4: 更新package.json脚本**

```json
{
  "scripts": {
    "prepare": "husky install",
    "lint": "turbo run lint",
    "lint:fix": "turbo run lint:fix",
    "format": "prettier --write \"**/*.{ts,tsx,json,md}\"",
    "check": "npm run format && npm run lint"
  }
}
```

**Step 5: Commit**

```bash
git add .env*.example .husky/ commitlint.config.js package.json
git commit -m "chore: 配置环境变量和Git Hooks"
```

---

### Task 24: 添加国际化支持（可选）

**Files:**
- Create: `apps/admin/src/locales/zh-CN.ts`
- Create: `apps/admin/src/locales/en-US.ts`
- Create: `apps/admin/src/hooks/useLocale.ts`
- Create: `apps/admin/src/components/LocaleProvider.tsx`
- Modify: `apps/admin/src/app/layout.tsx`

**Step 1: 创建语言包**

```typescript
// apps/admin/src/locales/zh-CN.ts
export const zhCN = {
  common: {
    login: '登录',
    logout: '退出',
    save: '保存',
    cancel: '取消',
    confirm: '确认',
    delete: '删除',
    edit: '编辑',
    add: '添加',
    search: '搜索',
    reset: '重置',
    loading: '加载中...',
    noData: '暂无数据',
    success: '操作成功',
    error: '操作失败',
  },
  menu: {
    dashboard: '仪表盘',
    orders: '订单管理',
    dispatch: '调度管理',
    transport: '运输管理',
    customers: '客户管理',
    settings: '系统设置',
  },
  order: {
    title: '订单管理',
    create: '创建订单',
    detail: '订单详情',
    orderNo: '订单号',
    customer: '客户',
    status: '状态',
    amount: '金额',
    pickupAddress: '取件地址',
    deliveryAddress: '送达地址',
  },
};
```

```typescript
// apps/admin/src/locales/en-US.ts
export const enUS = {
  common: {
    login: 'Login',
    logout: 'Logout',
    save: 'Save',
    cancel: 'Cancel',
    confirm: 'Confirm',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    loading: 'Loading...',
    noData: 'No data',
    success: 'Success',
    error: 'Error',
  },
  menu: {
    dashboard: 'Dashboard',
    orders: 'Orders',
    dispatch: 'Dispatch',
    transport: 'Transport',
    customers: 'Customers',
    settings: 'Settings',
  },
  order: {
    title: 'Order Management',
    create: 'Create Order',
    detail: 'Order Detail',
    orderNo: 'Order No',
    customer: 'Customer',
    status: 'Status',
    amount: 'Amount',
    pickupAddress: 'Pickup Address',
    deliveryAddress: 'Delivery Address',
  },
};
```

**Step 2: 创建Locale Hook和Provider**

```typescript
// apps/admin/src/hooks/useLocale.ts
import { useContext } from 'react';
import { LocaleContext } from '@/components/LocaleProvider';
import zhCN from '@/locales/zh-CN';
import enUS from '@/locales/en-US';

export type Locale = 'zh-CN' | 'en-US';

export const locales: Record<Locale, typeof zhCN> = {
  'zh-CN': zhCN,
  'en-US': enUS,
};

export const localeNames: Record<Locale, string> = {
  'zh-CN': '简体中文',
  'en-US': 'English',
};

export function useLocale() {
  const context = useContext(LocaleContext);
  if (!context) {
    throw new Error('useLocale must be used within LocaleProvider');
  }
  return context;
}
```

```typescript
// apps/admin/src/components/LocaleProvider.tsx
'use client';

import { createContext, useContext, useState, ReactNode } from 'react';
import { Locale, locales } from '@/hooks/useLocale';

interface Props {
  children: ReactNode;
}

interface LocaleContextType {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  t: typeof locales['zh-CN'];
}

export const LocaleContext = createContext<LocaleContextType | null>(null);

export function LocaleProvider({ children }: Props) {
  const [locale, setLocale] = useState<Locale>('zh-CN');

  return (
    <LocaleContext.Provider
      value={{
        locale,
        setLocale,
        t: locales[locale],
      }}
    >
      {children}
    </LocaleContext.Provider>
  );
}
```

**Step 3: 更新根布局**

```tsx
// apps/admin/src/app/layout.tsx
import { LocaleProvider } from '@/components/LocaleProvider';

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <LocaleProvider>{children}</LocaleProvider>
      </body>
    </html>
  );
}
```

**Step 4: Commit**

```bash
git add apps/admin/src/locales/ apps/admin/src/hooks/ apps/admin/src/components/LocaleProvider.tsx
git commit -m "feat(i18n): 添加国际化支持"
```

---

## 总结

本实施计划包含 **24个Task**，分布在9个Phase中：

| Phase | 任务数 | 内容 |
|-------|-------|------|
| Phase 1 | 4 | 项目初始化、Redux配置、布局组件 |
| Phase 2 | 3 | 订单管理（列表、创建、详情） |
| Phase 3 | 1 | 调度管理 |
| Phase 4 | 1 | 运输管理 |
| Phase 5 | 1 | 客户管理 |
| Phase 6 | 1 | 仪表盘 |
| Phase 7 | 3 | 移动端司机端（初始化、登录、首页） |
| Phase 8 | 1 | 测试与优化 |
| Phase 9 | 8 | 核心功能补充（Review后新增） |

### Phase 9 新增内容：
- **Task 17**: API拦截器和路由守卫
- **Task 18**: 移动端真实API集成
- **Task 19**: 移动端任务详情页
- **Task 20**: 移动端扫码功能
- **Task 21**: 移动端个人中心页
- **Task 22**: 错误边界和全局错误处理
- **Task 23**: 环境变量和Git Hooks配置
- **Task 24**: 国际化支持（可选）

---

## Phase 10: 核心功能补充（二次Review新增）

### Task 25: 代码问题修复和优化

**Files:**
- Modify: `apps/admin/src/app/(main)/dashboard/page.tsx`
- Modify: `apps/admin/src/middleware.ts` (明确路径)

**Step 1: 修复Dashboard导入**

```tsx
// apps/admin/src/app/(main)/dashboard/page.tsx
import { Row, Col, Card, Statistic, List, Tag, Typography, Spin } from 'antd';
import {
  ShoppingCartOutlined,
  CarOutlined,
  ArrowUpOutlined,
  ClockCircleOutlined
} from '@ant-design/icons';
// 修复 ClockCircleOutlined 导入缺失
```

**Step 2: 修复类型安全问题**

```typescript
// 将所有 any 类型替换为具体类型
// Table onChange 参数类型
onChange={(pagination, filters, sorter, extra) => {
  // 添加正确的类型注解
}}
```

**Step 3: 添加Loading状态组件**

```tsx
// apps/admin/src/components/LoadingSpinner/index.tsx
import { Spin } from 'antd';

export const LoadingSpinner = ({ tip = '加载中...' }: { tip?: string }) => (
  <div style={{ textAlign: 'center', padding: 100 }}>
    <Spin size="large" tip={tip} />
  </div>
);
```

**Step 4: 添加空状态组件**

```tsx
// apps/admin/src/components/EmptyState/index.tsx
import { Result, Button } from 'antd';

interface EmptyStateProps {
  title?: string;
  description?: string;
  onAction?: () => void;
  actionText?: string;
}

export const EmptyState = ({
  title = '暂无数据',
  description,
  onAction,
  actionText,
}: EmptyStateProps) => (
  <Result
    status="info"
    title={title}
    subTitle={description}
    extra={onAction && actionText ? (
      <Button type="primary" onClick={onAction}>{actionText}</Button>
    ) : undefined}
  />
);
```

**Step 5: Commit**

```bash
git add apps/admin/src/app/\(main\)/dashboard/page.tsx apps/admin/src/components/
git commit -m "fix: 修复Dashboard导入和添加通用组件"
```

---

### Task 26: 实现司机管理模块

**Files:**
- Create: `apps/admin/src/types/driver.ts`
- Create: `apps/admin/src/features/transport/api/driverApi.ts`
- Create: `apps/admin/src/features/transport/components/DriverList/index.tsx`
- Create: `apps/admin/src/features/transport/components/DriverForm/index.tsx`
- Create: `apps/admin/src/app/(main)/transport/drivers/page.tsx`

**Step 1: 创建司机类型定义**

```typescript
// apps/admin/src/types/driver.ts
export type DriverStatus = 'IDLE' | 'ON_DUTY' | 'OFF_DUTY' | 'ON_LEAVE';

export const DRIVER_STATUS_TEXT: Record<DriverStatus, string> = {
  IDLE: '空闲',
  ON_DUTY: '出车中',
  OFF_DUTY: '下班',
  ON_LEAVE: '请假',
};

export interface Driver {
  id: string;
  name: string;
  phone: string;
  licenseNumber: string;
  status: DriverStatus;
  vehicleId?: string;
  vehiclePlateNumber?: string;
  hiredDate: string;
  lastActiveAt?: string;
}

export interface CreateDriverRequest {
  name: string;
  phone: string;
  licenseNumber: string;
  hiredDate: string;
}
```

**Step 2: 创建司机API**

```typescript
// apps/admin/src/features/transport/api/driverApi.ts
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { Driver, CreateDriverRequest } from '@/types/driver';

export const driverApi = createApi({
  reducerPath: 'driverApi',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api/v1/drivers',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as { auth: { token: string | null } }).auth.token;
      if (token) headers.set('Authorization', `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ['Driver'],
  endpoints: (builder) => ({
    getDrivers: builder.query<{ items: Driver[]; total: number }, { status?: string; pageNum: number; pageSize: number }>({
      query: (params) => ({ url: '', params }),
      providesTags: [{ type: 'Driver', id: 'LIST' }],
    }),
    getDriver: builder.query<Driver, string>({
      query: (id) => `/${id}`,
      providesTags: (_, __, id) => [{ type: 'Driver', id }],
    }),
    createDriver: builder.mutation<Driver, CreateDriverRequest>({
      query: (body) => ({ method: 'POST', body }),
      invalidatesTags: [{ type: 'Driver', id: 'LIST' }],
    }),
    updateDriver: builder.mutation<Driver, { id: string; data: Partial<Driver> }>({
      query: ({ id, data }) => ({ url: `/${id}`, method: 'PATCH', body: data }),
      invalidatesTags: (_, __, { id }) => [{ type: 'Driver', id }, { type: 'Driver', id: 'LIST' }],
    }),
  }),
});

export const { useGetDriversQuery, useGetDriverQuery, useCreateDriverMutation, useUpdateDriverMutation } = driverApi;
```

**Step 3: 创建司机列表组件**

```tsx
// apps/admin/src/features/transport/components/DriverList/index.tsx
'use client';

import { useState } from 'react';
import { Table, Tag, Card, Typography, Row, Col, Statistic, Button, Space, message, Modal } from 'antd';
import { UserOutlined, PlusOutlined, EditOutlined, PhoneOutlined } from '@ant-design/icons';
import { useGetDriversQuery, useUpdateDriverMutation } from '@/features/transport/api/driverApi';
import type { Driver, DriverStatus } from '@/types/driver';
import { DriverForm } from '../DriverForm';
import dayjs from 'dayjs';

const { Title } = Typography;

export const DriverList = () => {
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDriver, setEditingDriver] = useState<Driver | null>(null);
  const { data, isLoading, refetch } = useGetDriversQuery({
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
  });
  const [updateDriver] = useUpdateDriverMutation();

  const handleStatusChange = async (driverId: string, status: DriverStatus) => {
    try {
      await updateDriver({ id: driverId, data: { status } }).unwrap();
      message.success('状态更新成功');
      refetch();
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  const columns = [
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
      render: (name: string) => (
        <Space>
          <UserOutlined />
          {name}
        </Space>
      ),
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 120,
      render: (phone: string) => (
        <Space>
          <PhoneOutlined />
          {phone}
        </Space>
      ),
    },
    {
      title: '驾驶证号',
      dataIndex: 'licenseNumber',
      key: 'licenseNumber',
      width: 160,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: DriverStatus) => {
        const statusMap: Record<string, { color: string; text: string }> = {
          IDLE: { color: 'green', text: '空闲' },
          ON_DUTY: { color: 'blue', text: '出车中' },
          OFF_DUTY: { color: 'default', text: '下班' },
          ON_LEAVE: { color: 'orange', text: '请假' },
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '当前车辆',
      dataIndex: 'vehiclePlateNumber',
      key: 'vehicle',
      width: 100,
      render: (text: string) => text || '-',
    },
    {
      title: '入职日期',
      dataIndex: 'hiredDate',
      key: 'hiredDate',
      width: 120,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: unknown, record: Driver) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => {
              setEditingDriver(record);
              setModalVisible(true);
            }}
          >
            编辑
          </Button>
          {record.status === 'IDLE' && (
            <Button
              type="link"
              size="small"
              onClick={() => handleStatusChange(record.id, 'ON_DUTY')}
            >
              出车
            </Button>
          )}
          {record.status === 'ON_DUTY' && (
            <Button
              type="link"
              size="small"
              onClick={() => handleStatusChange(record.id, 'IDLE')}
            >
              收车
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const stats = {
    total: data?.total || 0,
    idle: data?.items?.filter(d => d.status === 'IDLE').length || 0,
    onDuty: data?.items?.filter(d => d.status === 'ON_DUTY').length || 0,
  };

  return (
    <Card>
      <div className="page-header">
        <Title level={4} style={{ margin: 0 }}>司机管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => {
          setEditingDriver(null);
          setModalVisible(true);
        }}>
          添加司机
        </Button>
      </div>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Statistic title="司机总数" value={stats.total} prefix={<UserOutlined />} />
        </Col>
        <Col span={6}>
          <Statistic title="空闲司机" value={stats.idle} valueStyle={{ color: '#52c41a' }} />
        </Col>
        <Col span={6}>
          <Statistic title="出车中" value={stats.onDuty} valueStyle={{ color: '#1890ff' }} />
        </Col>
      </Row>

      <Table
        columns={columns}
        dataSource={data?.items}
        loading={isLoading}
        rowKey="id"
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: data?.total || 0,
          showSizeChanger: true,
        }}
        onChange={(p) => setPagination(p as any)}
        scroll={{ x: 1000 }}
      />

      <Modal
        title={editingDriver ? '编辑司机' : '添加司机'}
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setEditingDriver(null);
        }}
        footer={null}
        width={600}
      >
        <DriverForm
          initialValues={editingDriver}
          onSuccess={() => {
            setModalVisible(false);
            setEditingDriver(null);
            refetch();
          }}
        />
      </Modal>
    </Card>
  );
};
```

**Step 4: 创建司机表单组件**

```tsx
// apps/admin/src/features/transport/components/DriverForm/index.tsx
import { Form, Input, DatePicker, Button, message } from 'antd';
import type { Driver, CreateDriverRequest } from '@/types/driver';
import { useCreateDriverMutation, useUpdateDriverMutation } from '@/features/transport/api/driverApi';
import dayjs from 'dayjs';

interface DriverFormProps {
  initialValues?: Driver | null;
  onSuccess: () => void;
}

export const DriverForm = ({ initialValues, onSuccess }: DriverFormProps) => {
  const [form] = Form.useForm();
  const [createDriver, { isLoading: creating }] = useCreateDriverMutation();
  const [updateDriver, { isLoading: updating }] = useUpdateDriverMutation();

  const handleFinish = async (values: Record<string, unknown>) => {
    try {
      const data: CreateDriverRequest = {
        name: values.name as string,
        phone: values.phone as string,
        licenseNumber: values.licenseNumber as string,
        hiredDate: (values.hiredDate as dayjs.Dayjs).toISOString(),
      };

      if (initialValues) {
        await updateDriver({ id: initialValues.id, data }).unwrap();
        message.success('司机信息更新成功');
      } else {
        await createDriver(data).unwrap();
        message.success('司机添加成功');
      }
      onSuccess();
    } catch (error) {
      message.error('操作失败');
    }
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleFinish}
      initialValues={initialValues ? {
        ...initialValues,
        hiredDate: dayjs(initialValues.hiredDate),
      } : undefined}
    >
      <Form.Item
        name="name"
        label="姓名"
        rules={[{ required: true, message: '请输入姓名' }]}
      >
        <Input placeholder="请输入司机姓名" />
      </Form.Item>

      <Form.Item
        name="phone"
        label="手机号"
        rules={[
          { required: true, message: '请输入手机号' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
        ]}
      >
        <Input placeholder="请输入手机号" />
      </Form.Item>

      <Form.Item
        name="licenseNumber"
        label="驾驶证号"
        rules={[{ required: true, message: '请输入驾驶证号' }]}
      >
        <Input placeholder="请输入驾驶证号" />
      </Form.Item>

      <Form.Item
        name="hiredDate"
        label="入职日期"
        rules={[{ required: true, message: '请选择入职日期' }]}
      >
        <DatePicker style={{ width: '100%' }} />
      </Form.Item>

      <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
        <Space>
          <Button onClick={() => form.resetFields()}>重置</Button>
          <Button type="primary" htmlType="submit" loading={creating || updating}>
            {initialValues ? '更新' : '添加'}
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};
```

**Step 5: 创建司机管理页面**

```tsx
// apps/admin/src/app/(main)/transport/drivers/page.tsx
'use client';

import { DriverList } from '@/features/transport/components/DriverList';

export default function DriversPage() {
  return <DriverList />;
}
```

**Step 6: 更新Sidebar菜单**

```tsx
// apps/admin/src/components/layout/Sidebar.tsx
// 添加 drivers 路由
{
  key: '/transport/drivers',
  icon: <TeamOutlined />,
  label: '司机管理',
},
```

**Step 7: Commit**

```bash
git add apps/admin/src/types/driver.ts apps/admin/src/features/transport/api/driverApi.ts
git add apps/admin/src/features/transport/components/DriverList/ apps/admin/src/features/transport/components/DriverForm/
git add apps/admin/src/app/\(main\)/transport/drivers/
git commit -m "feat(transport): 实现司机管理模块"
```

---

### Task 27: 实现调度指派功能

**Files:**
- Create: `apps/admin/src/features/dispatch/components/DispatchAssignModal/index.tsx`
- Create: `apps/admin/src/features/dispatch/components/DispatchForm/index.tsx`
- Modify: `apps/admin/src/features/dispatch/components/DispatchBoard/index.tsx`

**Step 1: 创建调度指派弹窗**

```tsx
// apps/admin/src/features/dispatch/components/DispatchAssignModal/index.tsx
import { useState, useEffect } from 'react';
import { Modal, Form, Select, DatePicker, TimePicker, Button, message, Space, Card, Descriptions } from 'antd';
import type { Order } from '@/types/order';
import type { Vehicle, Driver } from '@/types/transport';
import { useGetVehiclesQuery } from '@/features/transport/api/vehicleApi';
import { useGetDriversQuery } from '@/features/transport/api/driverApi';
import { useCreateDispatchMutation } from '@/features/dispatch/api/dispatchApi';
import dayjs from 'dayjs';

interface DispatchAssignModalProps {
  order: Order | null;
  visible: boolean;
  onCancel: () => void;
  onSuccess: () => void;
}

export const DispatchAssignModal = ({ order, visible, onCancel, onSuccess }: DispatchAssignModalProps) => {
  const [form] = Form.useForm();
  const [createDispatch, { isLoading }] = useCreateDispatchMutation();
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);

  const { data: vehiclesData } = useGetVehiclesQuery({
    status: 'AVAILABLE',
    pageNum: 1,
    pageSize: 100,
  });
  const { data: driversData } = useGetDriversQuery({
    status: 'IDLE',
    pageNum: 1,
    pageSize: 100,
  });

  useEffect(() => {
    if (visible && order) {
      form.resetFields();
    }
  }, [visible, order, form]);

  const handleFinish = async (values: Record<string, unknown>) => {
    if (!order) return;

    try {
      await createDispatch({
        orderId: order.id,
        vehicleId: values.vehicleId as string,
        driverId: values.driverId as string,
        plannedStartTime: (values.date as dayjs.Dayjs).hour((values.time as dayjs.Dayjs).hour()).minute((values.time as dayjs.Dayjs).minute()).toISOString(),
        plannedEndTime: (values.date as dayjs.Dayjs).hour(18).minute(0).toISOString(),
      }).unwrap();
      message.success('调度指派成功');
      onSuccess();
    } catch (error) {
      message.error('调度指派失败');
    }
  };

  const vehicles = vehiclesData?.items || [];
  const drivers = driversData?.items || [];

  // 根据选中的车辆过滤可用司机
  const availableDrivers = selectedVehicle
    ? drivers.filter(d => !d.vehicleId || d.vehicleId === selectedVehicle.id)
    : drivers;

  return (
    <Modal
      title="调度指派"
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={700}
    >
      {order && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Descriptions column={2}>
            <Descriptions.Item label="订单号">{order.orderNo}</Descriptions.Item>
            <Descriptions.Item label="客户">{order.customerName}</Descriptions.Item>
            <Descriptions.Item label="取件">
              {order.pickupAddress.province}{order.pickupAddress.city}
            </Descriptions.Item>
            <Descriptions.Item label="送达">
              {order.deliveryAddress.province}{order.deliveryAddress.city}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      <Form form={form} layout="vertical" onFinish={handleFinish}>
        <Form.Item
          name="vehicleId"
          label="选择车辆"
          rules={[{ required: true, message: '请选择车辆' }]}
        >
          <Select
            placeholder="请选择车辆"
            options={vehicles.map(v => ({
              value: v.id,
              label: `${v.plateNumber} (${v.brand} ${v.model})`,
            }))}
            onChange={(value) => {
              const vehicle = vehicles.find(v => v.id === value);
              setSelectedVehicle(vehicle || null);
              form.setFieldsValue({ driverId: undefined });
            }}
            showSearch
            filterOption={(input, option) =>
              (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
          />
        </Form.Item>

        <Form.Item
          name="driverId"
          label="选择司机"
          rules={[{ required: true, message: '请选择司机' }]}
        >
          <Select
            placeholder="请选择司机"
            options={availableDrivers.map(d => ({
              value: d.id,
              label: `${d.name} (${d.phone})`,
            }))}
            disabled={!selectedVehicle}
            showSearch
            filterOption={(input, option) =>
              (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
          />
        </Form.Item>

        <Form.Item
          name="date"
          label="计划日期"
          rules={[{ required: true, message: '请选择计划日期' }]}
          initialValue={dayjs()}
        >
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          name="time"
          label="计划时间"
          rules={[{ required: true, message: '请选择计划时间' }]}
        >
          <TimePicker format="HH:mm" style={{ width: '100%' }} minuteStep={15} />
        </Form.Item>

        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={onCancel}>取消</Button>
            <Button type="primary" htmlType="submit" loading={isLoading}>
              确认指派
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
};
```

**Step 2: 更新调度看板添加指派功能**

```tsx
// apps/admin/src/features/dispatch/components/DispatchBoard/index.tsx
// 添加状态
const [assignModalVisible, setAssignModalVisible] = useState(false);
const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

// 添加指派按钮处理
const handleAssign = (order: Order) => {
  setSelectedOrder(order);
  setAssignModalVisible(true);
};

// 在表格操作列添加指派按钮
{
  title: '操作',
  key: 'action',
  render: (_: unknown, record: Dispatch) => (
    <Space>
      {record.status === 'CREATED' && (
        <Button
          type="primary"
          size="small"
          onClick={() => handleAssign({ id: record.orderId } as Order)}
        >
          指派
        </Button>
      )}
    </Space>
  ),
}

// 添加弹窗组件
<DispatchAssignModal
  order={selectedOrder}
  visible={assignModalVisible}
  onCancel={() => {
    setAssignModalVisible(false);
    setSelectedOrder(null);
  }}
  onSuccess={() => {
    setAssignModalVisible(false);
    setSelectedOrder(null);
    refetch();
  }}
/>
```

**Step 3: 在待调度订单添加快捷指派**

```tsx
// 在 DispatchBoard 中添加待调度订单列表
const pendingOrders = data?.items?.filter(d => d.status === 'CREATED') || [];

<Row gutter={16}>
  <Col span={12}>
    <Card title="待调度订单" size="small">
      <Table
        size="small"
        columns={[
          { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
          { title: '客户', dataIndex: 'customerName', key: 'customerName' },
          {
            title: '操作',
            key: 'action',
            render: (_: unknown, record: any) => (
              <Button size="small" type="primary" onClick={() => handleAssign(record)}>
                指派
              </Button>
            ),
          },
        ]}
        dataSource={pendingOrders}
        rowKey="id"
        pagination={false}
      />
    </Card>
  </Col>
  <Col span={12}>
    {/* 已有调度列表 */}
  </Col>
</Row>
```

**Step 4: Commit**

```bash
git add apps/admin/src/features/dispatch/components/DispatchAssignModal/
git commit -m "feat(dispatch): 实现调度指派功能"
```

---

### Task 28: 实现移动端送达确认页

**Files:**
- Create: `apps/driver/src/app/(main)/tasks/[id]/confirm.tsx`
- Create: `apps/driver/src/features/tasks/components/DeliveryForm/index.tsx`

**Step 1: 创建送达确认表单**

```tsx
// apps/driver/src/features/tasks/components/DeliveryForm/index.tsx
import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, Image, ScrollView, Alert, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { tasksApi } from '@/features/tasks/api/tasksApi';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import * as ImagePicker from 'expo-image-picker';

export const DeliveryForm = ({ taskId, onSuccess }: { taskId: string; onSuccess: () => void }) => {
  const router = useRouter();
  const { user } = useAuthStore();
  const [signature, setSignature] = useState('');
  const [remark, setRemark] = useState('');
  const [photos, setPhotos] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);

  const pickImage = async () => {
    if (photos.length >= 3) {
      Alert.alert('提示', '最多上传3张照片');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsMultipleSelection: true,
      quality: 0.6,
    });

    if (!result.canceled && result.assets) {
      const newPhotos = result.assets.map(a => a.uri);
      setPhotos([...photos, ...newPhotos].slice(0, 3));
    }
  };

  const handleSubmit = async () => {
    if (!signature.trim()) {
      Alert.alert('提示', '请输入收货人签名');
      return;
    }

    setUploading(true);
    try {
      // 上传照片
      const uploadedPhotos: string[] = [];
      for (const photo of photos) {
        const formData = new FormData();
        formData.append('file', {
          uri: photo,
          name: `photo_${Date.now()}.jpg`,
          type: 'image/jpeg',
        } as any);
        const result = await tasksApi.uploadProof(taskId, formData);
        uploadedPhotos.push(result.url);
      }

      // 确认送达
      await tasksApi.confirmDelivery(taskId, signature, uploadedPhotos);
      Alert.alert('成功', '送达确认成功', [{ text: '确定', onPress: onSuccess }]);
    } catch (error) {
      Alert.alert('错误', '确认失败，请重试');
    } finally {
      setUploading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView style={styles.scrollView}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>收货人签名</Text>
          <View style={styles.signatureArea}>
            <TextInput
              style={styles.signatureInput}
              placeholder="请输入收货人姓名"
              value={signature}
              onChangeText={setSignature}
            />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>上传照片（可选）</Text>
          <Text style={styles.sectionDesc}>最多上传3张送达凭证照片</Text>
          <View style={styles.photosContainer}>
            {photos.map((photo, index) => (
              <Image key={index} source={{ uri: photo }} style={styles.photo} />
            ))}
            {photos.length < 3 && (
              <TouchableOpacity style={styles.addPhoto} onPress={pickImage}>
                <Text style={styles.addPhotoText}>+</Text>
                <Text style={styles.addPhotoLabel}>添加照片</Text>
              </TouchableOpacity>
            )}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>备注（可选）</Text>
          <TextInput
            style={styles.remarkInput}
            placeholder="请输入备注信息"
            value={remark}
            onChangeText={setRemark}
            multiline
            numberOfLines={3}
          />
        </View>

        <TouchableOpacity
          style={styles.submitButton}
          onPress={handleSubmit}
          disabled={uploading}
        >
          <Text style={styles.submitButtonText}>
            {uploading ? '确认中...' : '确认送达'}
          </Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
    padding: 16,
  },
  section: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 12,
  },
  sectionDesc: {
    fontSize: 12,
    color: '#999',
    marginBottom: 12,
  },
  signatureArea: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
  },
  signatureInput: {
    fontSize: 16,
    color: '#333',
    minHeight: 44,
  },
  photosContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  photo: {
    width: 100,
    height: 100,
    borderRadius: 8,
  },
  addPhoto: {
    width: 100,
    height: 100,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    borderStyle: 'dashed',
    justifyContent: 'center',
    alignItems: 'center',
  },
  addPhotoText: {
    fontSize: 32,
    color: '#999',
  },
  addPhotoLabel: {
    fontSize: 12,
    color: '#999',
  },
  remarkInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    textAlignVertical: 'top',
    minHeight: 80,
  },
  submitButton: {
    height: 48,
    backgroundColor: '#52c41a',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 16,
  },
  submitButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
```

**Step 2: 创建送达确认页面**

```tsx
// apps/driver/src/app/(main)/tasks/[id]/confirm.tsx
import { useLocalSearchParams } from 'expo-router';
import { DeliveryForm } from '@/features/tasks/components/DeliveryForm';
import { useRouter } from 'expo-router';

export default function ConfirmDeliveryScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();

  const handleSuccess = () => {
    router.replace('/(main)/home');
  };

  return <DeliveryForm taskId={id} onSuccess={handleSuccess} />;
}
```

**Step 3: Commit**

```bash
git add apps/driver/src/app/\(main\)/tasks/\[id\]/confirm.tsx
git add apps/driver/src/features/tasks/components/DeliveryForm/
git commit -m "feat(driver): 实现移动端送达确认页"
```

---

### Task 29: 实现数据导出功能

**Files:**
- Create: `apps/admin/src/utils/export.ts`
- Modify: `apps/admin/src/features/orders/components/OrderList/index.tsx`
- Modify: `apps/admin/src/features/dispatch/components/DispatchBoard/index.tsx`

**Step 1: 创建导出工具函数**

```typescript
// apps/admin/src/utils/export.ts
import dayjs from 'dayjs';

// 导出CSV
export function exportToCSV<T extends Record<string, unknown>>(
  data: T[],
  headers: Record<string, string>,
  filename: string
): void {
  const headerRow = Object.values(headers).join(',');
  const rows = data.map(item =>
    Object.keys(headers).map(key => {
      const value = item[key];
      const cell = value === null || value === undefined ? '' : String(value);
      // 处理包含逗号或换行的值
      if (cell.includes(',') || cell.includes('\n') || cell.includes('"')) {
        return `"${cell.replace(/"/g, '""')}"`;
      }
      return cell;
    }).join(',')
  );
  const csv = [headerRow, ...rows].join('\n');
  downloadFile(csv, `${filename}_${dayjs().format('YYYYMMDD_HHmmss')}.csv`, 'text/csv');
}

// 导出Excel (使用xlsx库)
export async function exportToExcel<T extends Record<string, unknown>>(
  data: T[],
  headers: Record<string, string>,
  filename: string,
  sheetName: string = 'Sheet1'
): Promise<void> {
  const { utils, writeFile } = await import('xlsx');

  // 转换数据格式
  const wsData = [
    Object.values(headers), // 表头
    ...data.map(item => Object.keys(headers).map(key => item[key])), // 数据行
  ];

  const ws = utils.aoa_to_sheet(wsData);
  const wb = utils.book_new();
  utils.book_append_sheet(wb, ws, sheetName);

  // 设置列宽
  const colWidths = Object.keys(headers).map(key => ({
    wch: Math.max(String(headers[key]).length, 15),
  }));
  ws['!cols'] = colWidths;

  writeFile(wb, `${filename}_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`);
}

// 下载文件辅助函数
function downloadFile(content: string, filename: string, mimeType: string): void {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
```

**Step 2: 在订单列表添加导出功能**

```tsx
// apps/admin/src/features/orders/components/OrderList/index.tsx
import { exportToExcel } from '@/utils/export';

// 在页面头部添加导出按钮
<Button icon={<DownloadOutlined />} onClick={handleExport}>
  导出
</Button>

// 添加导出处理函数
const handleExport = async () => {
  if (!data?.items?.length) {
    message.warning('暂无数据可导出');
    return;
  }

  try {
    await exportToExcel(
      data.items.map(item => ({
        orderNo: item.orderNo,
        customerName: item.customerName,
        status: ORDER_STATUS_TEXT[item.status as keyof typeof ORDER_STATUS_TEXT],
        pickupAddress: `${item.pickupAddress.province}${item.pickupAddress.city}${item.pickupAddress.detail}`,
        deliveryAddress: `${item.deliveryAddress.province}${item.deliveryAddress.city}${item.deliveryAddress.detail}`,
        totalAmount: item.totalAmount,
        createdAt: dayjs(item.createdAt).format('YYYY-MM-DD HH:mm:ss'),
      })),
      {
        orderNo: '订单号',
        customerName: '客户',
        status: '状态',
        pickupAddress: '取件地址',
        deliveryAddress: '送达地址',
        totalAmount: '金额',
        createdAt: '创建时间',
      },
      '订单数据'
    );
    message.success('导出成功');
  } catch (error) {
    message.error('导出失败');
  }
};
```

**Step 3: Commit**

```bash
git add apps/admin/src/utils/export.ts apps/admin/src/features/orders/components/OrderList/
git commit -m "feat: 实现数据导出功能"
```

---

### Task 30: 实现WebSocket实时推送

**Files:**
- Create: `apps/admin/src/services/socket.ts`
- Create: `apps/admin/src/hooks/useSocket.ts`
- Modify: `apps/admin/src/app/layout.tsx`

**Step 1: 创建WebSocket服务**

```typescript
// apps/admin/src/services/socket.ts
import { message } from 'antd';

type EventCallback = (data: unknown) => void;

class SocketService {
  private | null = null ws: WebSocket;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private eventListeners: Map<string, EventCallback[]> = new Map();
  private token: string | null = null;

  connect(token: string): void {
    this.token = token;
    const wsUrl = `${process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080'}/ws?token=${token}`;

    try {
      this.ws = new WebSocket(wsUrl);
      this.setupEventHandlers();
    } catch (error) {
      console.error('WebSocket连接失败:', error);
    }
  }

  private setupEventHandlers(): void {
    if (!this.ws) return;

    this.ws.onopen = () => {
      console.log('WebSocket已连接');
      this.reconnectAttempts = 0;
      this.subscribe('notifications', (data) => {
        const notification = data as { type: string; title: string; message: string };
        switch (notification.type) {
          case 'order_created':
            message.info(`新订单: ${notification.title}`);
            break;
          case 'order_status_changed':
            message.info(`订单状态更新: ${notification.title}`);
            break;
          case 'dispatch_assigned':
            message.info(`新调度任务: ${notification.title}`);
            break;
          default:
            message.info(notification.message);
        }
        this.emit('notification', notification);
      });
    };

    this.ws.onclose = () => {
      console.log('WebSocket已断开');
      this.attemptReconnect();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error);
    };

    this.ws.onmessage = (event) => {
      try {
        const { event: eventName, data } = JSON.parse(event.data);
        this.emit(eventName, data);
      } catch (error) {
        console.error('消息解析失败:', error);
      }
    };
  }

  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('WebSocket重连次数已达上限');
      return;
    }

    this.reconnectAttempts++;
    console.log(`尝试重连 WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      if (this.token) {
        this.connect(this.token);
      }
    }, this.reconnectDelay);
  }

  subscribe(event: string, callback: EventCallback): () => void {
    const listeners = this.eventListeners.get(event) || [];
    listeners.push(callback);
    this.eventListeners.set(event, listeners);

    // 返回取消订阅函数
    return () => {
      const eventListeners = this.eventListeners.get(event);
      if (eventListeners) {
        const index = eventListeners.indexOf(callback);
        if (index > -1) {
          eventListeners.splice(index, 1);
        }
      }
    };
  }

  private emit(event: string, data: unknown): void {
    const listeners = this.eventListeners.get(event);
    if (listeners) {
      listeners.forEach(callback => callback(data));
    }
  }

  send(event: string, data: unknown): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ event, data }));
    }
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.eventListeners.clear();
  }
}

export const socketService = new SocketService();
```

**Step 2: 创建Socket Hook**

```typescript
// apps/admin/src/hooks/useSocket.ts
import { useEffect, useRef } from 'react';
import { socketService } from '@/services/socket';

export function useSocket(event: string, callback: (data: unknown) => void) {
  const callbackRef = useRef(callback);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    const unsubscribe = socketService.subscribe(event, callbackRef.current);
    return unsubscribe;
  }, [event]);
}

export function useSocketConnected() {
  return useRef(false);
}
```

**Step 3: 在根布局初始化连接**

```tsx
// apps/admin/src/app/layout.tsx
'use client';

import { useEffect } from 'react';
import { Providers } from '@/components/Providers';
import { socketService } from '@/services/socket';
import { useAppSelector } from '@/store/hooks';

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const { token } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (token) {
      socketService.connect(token);
    }

    return () => {
      socketService.disconnect();
    };
  }, [token]);

  return (
    <html lang="zh-CN">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
```

**Step 4: 在仪表盘使用实时更新**

```tsx
// apps/admin/src/app/(main)/dashboard/page.tsx
import { useSocket } from '@/hooks/useSocket';

export default function DashboardPage() {
  // 订阅订单更新
  useSocket('order_updated', (data) => {
    // 刷新数据
    refetch();
  });

  // 订阅新订单
  useSocket('order_created', (data) => {
    message.info('有新订单');
    refetch();
  });
}
```

**Step 5: Commit**

```bash
git add apps/admin/src/services/socket.ts apps/admin/src/hooks/useSocket.ts
git commit -m "feat: 实现WebSocket实时推送"
```

---

### Task 31: 实现骨架屏和加载状态优化

**Files:**
- Create: `apps/admin/src/components/Skeleton/OrderSkeleton.tsx`
- Create: `apps/admin/src/components/Skeleton/TableSkeleton.tsx`
- Create: `apps/admin/src/components/Skeleton/index.ts`
- Modify: 各列表页面使用骨架屏

**Step 1: 创建骨架屏组件**

```typescript
// apps/admin/src/components/Skeleton/index.ts
export { OrderSkeleton } from './OrderSkeleton';
export { TableSkeleton } from './TableSkeleton';
```

```typescript
// apps/admin/src/components/Skeleton/TableSkeleton.tsx
import { Skeleton, Card } from 'antd';

interface TableSkeletonProps {
  rows?: number;
  columns?: number;
}

export const TableSkeleton = ({ rows = 5, columns = 5 }: TableSkeletonProps) => {
  return (
    <Card>
      <div style={{ marginBottom: 16 }}>
        <Skeleton.Input active size="small" style={{ width: 200 }} />
      </div>
      <Skeleton.Button active block style={{ height: 40 }} />
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <div key={rowIndex} style={{ display: 'flex', gap: 8, marginTop: 16 }}>
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Skeleton.Input
              key={colIndex}
              active
              style={{ flex: 1, height: 20 }}
            />
          ))}
        </div>
      ))}
    </Card>
  );
};
```

```typescript
// apps/admin/src/components/Skeleton/OrderSkeleton.tsx
import { Skeleton, Card, Row, Col } from 'antd';

export const OrderSkeleton = () => {
  return (
    <Card>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        {Array.from({ length: 4 }).map((_, index) => (
          <Col key={index} span={6}>
            <Skeleton.Input active style={{ width: '100%', height: 80 }} />
          </Col>
        ))}
      </Row>
      <Skeleton.Input active style={{ width: 200, height: 32, marginBottom: 16 }} />
      <TableSkeleton rows={5} columns={6} />
    </Card>
  );
};
```

**Step 2: 在Dashboard使用**

```tsx
// apps/admin/src/app/(main)/dashboard/page.tsx
import { Skeleton } from 'antd';
import { OrderSkeleton } from '@/components/Skeleton';

export default function DashboardPage() {
  if (loading) {
    return <OrderSkeleton />;
  }
}
```

**Step 3: 在订单列表使用**

```tsx
// apps/admin/src/features/orders/components/OrderList/index.tsx
import { TableSkeleton } from '@/components/Skeleton';

export const OrderList = () => {
  const { data, isLoading } = useGetOrdersQuery({...});

  if (isLoading) {
    return <TableSkeleton rows={10} columns={7} />;
  }
}
```

**Step 4: Commit**

```bash
git add apps/admin/src/components/Skeleton/
git commit -m "feat: 添加骨架屏加载状态组件"
```

---

## 总结

本实施计划包含 **31个Task**，分布在10个Phase中：

| Phase | 任务数 | 内容 |
|-------|-------|------|
| Phase 1 | 4 | 项目初始化、Redux配置、布局组件 |
| Phase 2 | 3 | 订单管理（列表、创建、详情） |
| Phase 3 | 1 | 调度管理 |
| Phase 4 | 1 | 运输管理 |
| Phase 5 | 1 | 客户管理 |
| Phase 6 | 1 | 仪表盘 |
| Phase 7 | 3 | 移动端司机端（初始化、登录、首页） |
| Phase 8 | 1 | 测试与优化 |
| Phase 9 | 8 | 核心功能补充（Review后新增） |
| Phase 10 | 7 | 核心功能补充（二次Review新增） |

### Phase 10 新增内容：
- **Task 25**: 代码问题修复和优化
- **Task 26**: 司机管理模块
- **Task 27**: 调度指派功能
- **Task 28**: 移动端送达确认页
- **Task 29**: 数据导出功能
- **Task 30**: WebSocket实时推送
- **Task 31**: 骨架屏和加载状态优化

---

**计划完成。**

**执行方式：**

1. **子驱动模式（推荐）** - 我在当前会话中逐个Task执行，任务间进行代码审查
2. **并行会话模式** - 您在新会话中使用 `superpowers:executing-plans` 批量执行

**选择哪种方式？**
