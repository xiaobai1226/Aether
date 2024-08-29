// import type {MenuChildren} from './MenuChildren';

/**
 * 菜单信息
 */
export interface Menu {
    icon: string;
    name: string;
    category?: string;
    path: string;
    allShow: boolean;
    menuCode: string;
    tips?: string;
    percent?: number;
    // children: MenuChildren[];
}