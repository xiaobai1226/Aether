import { ByteConversionFators } from '@/enums/ByteConversionFators'

export default {
  sizeToStr: (limit: number): string => {

    // 处理负数情况，先取绝对值进行换算，最后再根据原数正负添加符号
    const absLimit = Math.abs(limit)
    let size
    let unit

    if (absLimit < ByteConversionFators.KB) {
      size = absLimit.toFixed(2)
      unit = 'B'
    } else if (absLimit < ByteConversionFators.MB) {
      size = (absLimit / ByteConversionFators.KB).toFixed(2)
      unit = 'KB'
    } else if (absLimit < ByteConversionFators.GB) {
      size = (absLimit / ByteConversionFators.MB).toFixed(2)
      unit = 'MB'
    } else if (absLimit < ByteConversionFators.TB) {
      size = (absLimit / ByteConversionFators.GB).toFixed(2)
      unit = 'GB'
    } else {
      size = (absLimit / ByteConversionFators.TB).toFixed(2)
      unit = 'TB'
    }

    // 根据原数正负添加符号
    if (limit < 0) {
      size = '-' + size
    }

    // 当小数部分为0时，去掉小数部分
    if (Number(size) % 1 === 0) {
      size = size.split('.')[0]
    }

    return size + unit
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
  },

  extName: (name: string | undefined): string => {
    if (!name) {
      return ''
    }
    return name.split('.').pop() ?? ''
  }
}