export const generateToolbarContent = (barList, width) => {
  return `<div class="tea-overlay tea-transition-scale-691318344-enter-done" data-placement="bottom-start" style="position: absolute; top: 30px; margin-left: ${width -
    100}px;">
    <div class="tea-dropdown-box" style="position: relative;">
      <ul class="tea-list tea-list--option" style="max-height: initial;">
        ${barList.map((item, i) => `<li key="${i}" index="${i}" >${item.text}</li>`)}
      </ul>
    </div>
  </div>`;
};
