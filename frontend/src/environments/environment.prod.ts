export const environment = {
  production: true,
  apiUrl: (window as any).env?.apiUrl || 'http://localhost:8080' // Có thể thay đổi URL cho production
}; 