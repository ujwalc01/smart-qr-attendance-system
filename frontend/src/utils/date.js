export function toLocalInputValue(date = new Date()) {
  const copy = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return copy.toISOString().slice(0, 16);
}

export function toIsoFromLocal(value) {
  return new Date(value).toISOString();
}
