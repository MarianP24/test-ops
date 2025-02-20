class TreeMap {
    constructor() {
        this.zones = new Map();
        this.initialize();
    }

    initialize() {
        // Get all zone elements
        const zoneElements = document.querySelectorAll('.treemap-cell');
        zoneElements.forEach(zone => {
            const zoneId = zone.getAttribute('data-zone');
            const column = zone.closest('[data-column]');
            const totalZonesInColumn = column.querySelectorAll('.treemap-cell').length;
            const defaultHeight = (column.offsetHeight - (10 * (totalZonesInColumn - 1))) / totalZonesInColumn;

            this.zones.set(zoneId, {
                element: zone,
                column: column.getAttribute('data-column'),
                defaultHeight: defaultHeight,
                currentHeight: defaultHeight
            });

            zone.style.height = `${defaultHeight}px`;

            let hoverTimer;
            let closeTimer;
            let isPopupHovered = false;


            const popupMouseEnterHandler = () => {
                isPopupHovered = true;
                clearTimeout(closeTimer);
            };

            const popupMouseLeaveHandler = () => {
                isPopupHovered = false;
                handleMouseLeave();
            };

            const handleMouseLeave = () => {
                clearTimeout(hoverTimer);
                closeTimer = setTimeout(() => {
                    if (!isPopupHovered) {
                        zone.classList.remove('show-popup');
                        const popup = zone.querySelector('.popup') || zone;
                        popup.removeEventListener('mouseenter', popupMouseEnterHandler);
                        popup.removeEventListener('mouseleave', popupMouseLeaveHandler);
                    }
                }, 200);
            };


            zone.addEventListener('mouseenter', () => {
                clearTimeout(closeTimer);
                hoverTimer = setTimeout(() => {
                    zone.classList.add('show-popup');
                    const popup = zone.querySelector('.popup') || zone;
                    popup.addEventListener('mouseenter', popupMouseEnterHandler);
                    popup.addEventListener('mouseleave', popupMouseLeaveHandler);
                }, 1000);
            });

            zone.addEventListener('mouseleave', handleMouseLeave);

        });

        this.addResizeHandlers();

    }

    addResizeHandlers() {
        this.zones.forEach((zoneData, zoneId) => {
            const element = zoneData.element;

            // Make zone resizable
            element.style.resize = 'vertical';

            let resizeObserver = new ResizeObserver((entries) => {
                for (let entry of entries) {
                    const newHeight = entry.contentRect.height;
                    zoneData.currentHeight = newHeight;
                    this.updateLayout(zoneId);
                }
            });

            resizeObserver.observe(element);
        });
    }

    updateLayout(changedZoneId) {
        const changedZone = this.zones.get(changedZoneId);
        const column = changedZone.element.closest('[data-column]');
        const columnZones = Array.from(this.zones.values())
            .filter(zone => zone.element.closest('[data-column]') === column);

        // Calculate how much height changed
        const heightChange = changedZone.currentHeight - changedZone.defaultHeight;
        const remainingZones = columnZones.filter(zone =>
            zone.element.getAttribute('data-zone') !== changedZoneId);

        // Distribute the height change among other zones
        const heightAdjustment = heightChange / remainingZones.length;

        remainingZones.forEach(zone => {
            const newHeight = zone.defaultHeight - heightAdjustment;
            if (newHeight >= 50) { // Respect min-height from CSS
                zone.element.style.height = `${newHeight}px`;
                zone.currentHeight = newHeight;
            }
        });
    }

    // Method to manually update zone size
    updateZoneSize(zoneId, height) {
        const zone = this.zones.get(zoneId);
        if (zone && height >= 50) { // Respect min-height
            zone.element.style.height = `${height}px`;
            zone.currentHeight = height;
            this.updateLayout(zoneId);
        }
    }

}

// Initialize the treemap when the document is ready
document.addEventListener('DOMContentLoaded', () => {
    const treeMap = new TreeMap();

    treeMap.updateZoneSize('1', 100);
    treeMap.updateZoneSize('2', 100);
    treeMap.updateZoneSize('3', 200);
    treeMap.updateZoneSize('4', 200);
    treeMap.updateZoneSize('5', 200);
    treeMap.updateZoneSize('6', 100);
    treeMap.updateZoneSize('7', 100);
    treeMap.updateZoneSize('8', 415);
    treeMap.updateZoneSize('9', 200);
    treeMap.updateZoneSize('10', 115);

});