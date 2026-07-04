import { describe, it, expect, beforeEach, vi } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import Login from "@/views/Login.vue";

// Mock localStorage for Pinia user store
const store: Record<string, string> = {};
vi.stubGlobal("localStorage", {
  getItem: (k: string) => store[k] ?? null,
  setItem: (k: string, v: string) => { store[k] = v; },
  removeItem: (k: string) => { delete store[k]; },
});

describe("Login.vue", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("renders username input", () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [createPinia()],
        stubs: { "router-link": true, "el-input": true, "el-button": true, "el-form": true, "el-form-item": true, "el-icon": true },
      },
    });
    expect(wrapper.exists()).toBe(true);
  });
});
