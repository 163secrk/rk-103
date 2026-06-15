import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/userinfo',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function getMenus() {
  return request({
    url: '/menu',
    method: 'get'
  })
}

export function getAthletes(params) {
  return request({
    url: '/athletes',
    method: 'get',
    params
  })
}

export function getAthlete(id) {
  return request({
    url: `/athletes/${id}`,
    method: 'get'
  })
}

export function createAthlete(data) {
  return request({
    url: '/athletes',
    method: 'post',
    data
  })
}

export function updateAthlete(id, data) {
  return request({
    url: `/athletes/${id}`,
    method: 'put',
    data
  })
}

export function deleteAthlete(id) {
  return request({
    url: `/athletes/${id}`,
    method: 'delete'
  })
}

export function getInjuryRecords(params) {
  return request({
    url: '/injury-records',
    method: 'get',
    params
  })
}

export function getInjuryRecord(id) {
  return request({
    url: `/injury-records/${id}`,
    method: 'get'
  })
}

export function createInjuryRecord(data) {
  return request({
    url: '/injury-records',
    method: 'post',
    data
  })
}

export function updateInjuryRecord(id, data) {
  return request({
    url: `/injury-records/${id}`,
    method: 'put',
    data
  })
}

export function deleteInjuryRecord(id) {
  return request({
    url: `/injury-records/${id}`,
    method: 'delete'
  })
}

export function getRehabilitationPlansByInjury(injuryRecordId) {
  return request({
    url: `/rehabilitation-plans/injury/${injuryRecordId}`,
    method: 'get'
  })
}

export function getActiveRehabilitationPlan(injuryRecordId) {
  return request({
    url: `/rehabilitation-plans/injury/${injuryRecordId}/active`,
    method: 'get'
  })
}

export function getRehabilitationPlan(id) {
  return request({
    url: `/rehabilitation-plans/${id}`,
    method: 'get'
  })
}

export function createRehabilitationPlan(data) {
  return request({
    url: '/rehabilitation-plans',
    method: 'post',
    data
  })
}

export function updateRehabilitationPlan(id, data) {
  return request({
    url: `/rehabilitation-plans/${id}`,
    method: 'put',
    data
  })
}

export function deleteRehabilitationPlan(id) {
  return request({
    url: `/rehabilitation-plans/${id}`,
    method: 'delete'
  })
}
