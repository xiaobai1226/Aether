import {RegexEnum} from "@/enums/RegexEnum";

/**
 * 正则表达式校验方法
 */
const regexVerify = (rule: any, value: string, reg: RegExp, callback: Function) => {
    if (value) {
        if (reg.test(value)) {
            callback();
        } else {
            callback(new Error(rule.message));
        }
    } else {
        callback();
    }
}

// 校验用户名格式
const username = (rule: any, value: string, callback: Function) => {
    return regexVerify(rule, value, new RegExp(RegexEnum.REGEX_USERNAME), callback);
}

// 校验密码格式
const password = (rule: any, value: string, callback: Function) => {
    return regexVerify(rule, value, new RegExp(RegexEnum.REGEX_PASSWARD), callback);
}

// 校验提取码格式
const extractionCode = (rule: any, value: string, callback: Function) => {
    return regexVerify(rule, value, new RegExp(RegexEnum.REGEX_EXTRACTION_CODE), callback);
}

export default {
    username, password, extractionCode
}