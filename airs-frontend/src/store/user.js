import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api'
import { getToken, setToken, removeToken, setUserInfo, getUserInfo as getLocalUser } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getLocalUser() || null
  }),
  actions: {
    async login(userData) {
      const res = await login(userData)
      this.token = res.token
      this.userInfo = res.user
      setToken(res.token)
      setUserInfo(res.user)
      return res
    },
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res
      setUserInfo(res)
      return res
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.userInfo = null
        removeToken()
      }
    },
    hasRole(roles) {
      if (!this.userInfo) return false
      if (!Array.isArray(roles)) roles = [roles]
      return roles.includes(this.userInfo.role)
    }
  }
})
