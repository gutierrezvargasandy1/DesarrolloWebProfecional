import { environment } from "../../environments/environment";

export class UrlHelper {
  static file(path: string): string {
    return path ? `${environment.fileUrl}${path}` : '';
  }
}