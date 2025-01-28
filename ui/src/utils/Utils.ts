import { ByteConversionFators } from '@/enums/ByteConversionFators'


/**
 * 获取文件大小信息数组
 * @param limit
 */
const getSizeStrArray = (limit: number): Array<string> => {
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
  } else if (absLimit < ByteConversionFators.PB) {
    size = (absLimit / ByteConversionFators.TB).toFixed(2)
    unit = 'TB'
  } else if (absLimit < ByteConversionFators.EB) {
    size = (absLimit / ByteConversionFators.PB).toFixed(2)
    unit = 'PB'
  } else {
    size = (absLimit / ByteConversionFators.EB).toFixed(2)
    unit = 'EB'
  }

  // 根据原数正负添加符号
  if (limit < 0) {
    size = '-' + size
  }

  // 当小数部分为0时，去掉小数部分
  if (Number(size) % 1 === 0) {
    size = size.split('.')[0]
  }

  return [size, unit]
}

const sizeToStr = (limit: number): string => {
  const result = getSizeStrArray(limit)
  if (result && result.length == 2) {
    return result[0] + result[1]
  }

  return ''
}

const blobToBase64 = (blob: Blob): Promise<string> => {
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

const extName = (name: string | undefined): string => {
  if (!name) {
    return ''
  }
  return name.split('.').pop() ?? ''
}


export default {
  getSizeStrArray, sizeToStr, blobToBase64, extName
}