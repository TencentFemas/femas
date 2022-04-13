export default function resolvePath(...paths: string[]): string {
  return paths.join('/');
}
