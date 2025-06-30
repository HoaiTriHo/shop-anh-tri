# Shop App Frontend

A modern e-commerce frontend built with Angular 16+ and Bootstrap 5.

## Features

- **Modern UI**: Built with Bootstrap 5 for responsive, mobile-first design
- **Authentication**: JWT-based login/register system
- **Product Management**: Browse, search, and filter products
- **Shopping Cart**: Add items, manage quantities, and checkout
- **Admin Panel**: Product and order management for administrators
- **Responsive Design**: Works perfectly on desktop, tablet, and mobile

## Bootstrap Integration

This project uses Bootstrap 5 for styling and UI components:

### Installed Packages
- `bootstrap@5` - Core Bootstrap CSS and JavaScript
- `bootstrap-icons` - Bootstrap Icons for consistent iconography
- `@popperjs/core` - Required dependency for Bootstrap tooltips and popovers

### Configuration
- Bootstrap CSS is imported in `angular.json` styles array
- Bootstrap JavaScript is included in `angular.json` scripts array
- Bootstrap Icons CSS is imported for icon support
- Custom styles are defined in `src/styles.css`

### Key Bootstrap Components Used

#### Navigation
- Responsive navbar with collapsible menu
- Dropdown menus for user actions
- Active state indicators
- Mobile-friendly hamburger menu

#### Forms
- Form validation with visual feedback
- Input groups with buttons
- Custom form styling
- Responsive form layouts

#### Cards
- Product cards with hover effects
- Admin dashboard cards
- Feature showcase cards
- Consistent spacing and shadows

#### Buttons
- Custom button styling with rounded corners
- Loading states with spinners
- Icon integration
- Responsive button groups

#### Tables
- Responsive data tables
- Hover effects
- Sortable columns
- Action buttons

#### Modals & Alerts
- Bootstrap modals for forms
- Alert messages with icons
- Dismissible notifications
- Loading overlays

#### Utilities
- Flexbox utilities for layout
- Spacing utilities
- Text utilities
- Color utilities

## Custom Styling

### Global Styles (`src/styles.css`)
- Custom color scheme
- Typography improvements
- Utility classes
- Component-specific styles

### Custom Classes
- `.btn-custom` - Rounded buttons with custom padding
- `.form-container` - Centered form styling
- `.product-card` - Product card hover effects
- `.alert-custom` - Custom alert styling
- `.spinner-border-custom` - Custom loading spinner size

## Development

### Prerequisites
- Node.js 16+
- npm or yarn

### Installation
```bash
npm install
```

### Development Server
```bash
npm start
```
The app will be available at `http://localhost:4200`

### Build
```bash
npm run build
```

### Testing
```bash
npm test
```

## Project Structure

```
src/
├── app/
│   ├── admin/           # Admin panel components
│   ├── cart/            # Shopping cart components
│   ├── home/            # Home page components
│   ├── login/           # Authentication components
│   ├── products/        # Product listing components
│   ├── app.component.*  # Main app component
│   ├── app.module.ts    # App module
│   └── app-routing.ts   # Routing configuration
├── assets/              # Static assets
└── styles.css           # Global styles
```

## Bootstrap Best Practices

1. **Mobile-First**: All components are designed mobile-first
2. **Semantic HTML**: Using proper HTML5 semantic elements
3. **Accessibility**: ARIA labels and keyboard navigation
4. **Performance**: Optimized bundle size with tree-shaking
5. **Consistency**: Consistent spacing and typography

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## License

This project is licensed under the MIT License.
