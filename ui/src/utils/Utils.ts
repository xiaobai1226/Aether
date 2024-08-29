export default {
  sizeToStr: (limit: number): string => {
    let size: string = ''
    // 如果小于0.1KB转化成B
    if (limit < 0.1 * 1024) {
      size = limit.toFixed(2) + 'B'
    }
    // 如果小于0.1MB转化成KB
    else if (limit < 0.1 * 1024 * 1024) {
      size = (limit / 1024).toFixed(2) + 'KB'
    }
    // 如果小于0.1GB转化成MB
    else if (limit < 0.1 * 1024 * 1024 * 1024) {
      size = (limit / (1024 * 1024)).toFixed(2) + 'MB'
    }
    // 如果小于0.1TB转化成GB
    else if (limit < 0.1 * 1024 * 1024 * 1024 * 1024) {
      size = (limit / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
    }
    // 其他转化成TB
    else {
      size = (limit / (1024 * 1024 * 1024 * 1024)).toFixed(2) + 'TB'
    }

    // 转化成字符串
    const sizeStr = size + ''
    // 获取小数点处的索引
    const index = sizeStr.indexOf('.')
    // 获取小数点后两位的值
    const dou = sizeStr.substr(index + 1, 2)
    // 当小数点后为00时 去掉小数部分
    if (dou == '00') {
      return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
    }
    return size
  },

  blobToBase64: (blob: Blob): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()

      reader.onload = () => {
        if (reader.result) {
          resolve(reader.result as string)
        } else {
          reject(new Error('Failed to convert blob to base64'))
        }
      }

      reader.onerror = reject

      reader.readAsDataURL(blob)
    })
  }
}