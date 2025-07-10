# DSR Figma MCP Setup Guide
## Complete Implementation Guide for Figma Official Dev Mode MCP Server

### Overview
This guide provides step-by-step instructions for setting up Figma's Official Dev Mode MCP Server for the DSR frontend redesign project. The setup enables AI-assisted design-to-code workflows with comprehensive design token extraction and component mapping.

## Prerequisites Checklist

### ✅ Completed
- [x] Figma Desktop App installed (v125.6.4)
- [x] DSR frontend project with Next.js 14+ and Tailwind CSS
- [x] Git repository with feature branch `feature/frontend-redesign-figma`

### 📋 Required
- [ ] Figma account with Dev or Full seat (Professional/Organization/Enterprise plan)
- [ ] Figma personal access token
- [ ] VS Code or Cursor IDE with MCP support
- [ ] Node.js 18+ for MCP client configuration

## Step 1: Figma Account and Access Token Setup

### 1.1 Verify Figma Account Access
1. Open Figma Desktop App (installed at: `C:\Users\[Username]\AppData\Local\Figma\Figma.exe`)
2. Sign in with your Figma account
3. Verify you have a Dev or Full seat on Professional/Organization/Enterprise plan

### 1.2 Create Figma Personal Access Token
1. Go to Figma → Account Settings → Personal Access Tokens
2. Click "Create new token"
3. Name: "DSR MCP Server Token"
4. Scopes: Select "File content" and "Comments"
5. Copy the generated token (save securely - it won't be shown again)

**Important:** Store this token securely as it provides access to your Figma files.

## Step 2: Enable Dev Mode MCP Server in Figma

### 2.1 Enable MCP Server
1. Open Figma Desktop App
2. Create or open a DSR design file
3. Go to **Figma Menu** (upper-left corner)
4. Navigate to **Preferences**
5. Select **"Enable Dev Mode MCP Server"**
6. Confirm the server is running at `http://127.0.0.1:3845/sse`

### 2.2 Configure MCP Server Settings
1. In Preferences, go to **"Dev Mode MCP Server Settings"**
2. Enable the following options:
   - ✅ **Enable tool get_image** (for layout fidelity)
   - ✅ **Enable code connect** (for component mapping)
   - ✅ **Use placeholders** (initially, for faster iteration)

## Step 3: IDE Configuration

### For VS Code (Recommended for DSR)

#### 3.1 Install Required Extensions
```bash
# Install GitHub Copilot (required for MCP in VS Code)
code --install-extension GitHub.copilot
code --install-extension GitHub.copilot-chat
```

#### 3.2 Configure MCP Settings
1. Open VS Code
2. Go to **Code → Settings → Settings** (or `Ctrl+,`)
3. Search for "MCP"
4. Click **"Edit in settings.json"**
5. Add the following configuration:

```json
{
  "chat.mcp.discovery.enabled": true,
  "mcp": {
    "servers": {
      "Figma Dev Mode MCP": {
        "type": "sse",
        "url": "http://127.0.0.1:3845/sse"
      }
    }
  },
  "chat.agent.enabled": true
}
```

#### 3.3 Verify MCP Integration
1. Open chat toolbar using `Ctrl+Alt+I`
2. Switch to **Agent** mode
3. Open the **selection tool** menu
4. Look for section: `MCP Server: Figma Dev Mode MCP`
5. If no tools are listed, restart both Figma Desktop App and VS Code

### For Cursor (Alternative)

#### 3.1 Configure Cursor MCP
1. Open **Cursor → Settings → Cursor Settings**
2. Go to the **MCP** tab
3. Click **"+ Add new global MCP server"**
4. Enter configuration:

```json
{
  "mcpServers": {
    "Figma": {
      "url": "http://127.0.0.1:3845/sse"
    }
  }
}
```

## Step 4: DSR Design System Integration

### 4.1 Prepare Figma Files for DSR
Create or organize Figma files with the following structure:

```
DSR Design System/
├── 01-Design Tokens/
│   ├── Colors (Government brand colors)
│   ├── Typography (Inter font system)
│   ├── Spacing (DSR spacing scale)
│   └── Components (Status badges, buttons)
├── 02-User Role Interfaces/
│   ├── Citizen Interface/
│   ├── LGU Staff Interface/
│   ├── DSWD Staff Interface/
│   └── Admin Interface/
├── 03-Workflow Components/
│   ├── Registration Flow/
│   ├── Eligibility Assessment/
│   ├── Payment Processing/
│   └── Case Management/
└── 04-Responsive Layouts/
    ├── Mobile (320px-767px)/
    ├── Tablet (768px-1023px)/
    └── Desktop (1024px+)/
```

### 4.2 Set Up Design Variables in Figma
Map DSR design tokens to Figma variables:

```
Color Variables:
- primary/50 → #eff6ff (DSR primary blue scale)
- secondary/50 → #f0fdf4 (DSR secondary green scale)
- dsr-eligible → #10b981 (Status: eligible)
- dsr-pending → #f59e0b (Status: pending)
- dsr-processing → #3b82f6 (Status: processing)

Typography Variables:
- font-family-primary → Inter
- font-size-display-lg → 3rem
- line-height-display-lg → 1.1

Spacing Variables:
- space-micro → 0.25rem
- space-small → 0.5rem
- space-medium → 1rem
- space-large → 1.5rem
```

### 4.3 Configure Code Connect for DSR Components
For each DSR component, set up Code Connect mapping:

```typescript
// Example: Button component mapping
import { figma } from '@figma/code-connect'
import { Button } from '@/components/ui/button'

figma.connect(Button, "https://www.figma.com/file/[FILE_ID]?node-id=[NODE_ID]", {
  props: {
    variant: figma.enum("variant", {
      primary: "primary",
      secondary: "secondary",
      eligible: "eligible",
      pending: "pending"
    }),
    size: figma.enum("size", {
      sm: "sm",
      md: "md",
      lg: "lg"
    })
  },
  example: ({ variant, size }) => (
    <Button variant={variant} size={size}>
      {figma.instance("children")}
    </Button>
  )
})
```

## Step 5: Testing and Validation

### 5.1 Test MCP Connection
1. Open VS Code with DSR project
2. Open Agent chat (`Ctrl+Alt+I`)
3. Test with a simple prompt:
   ```
   "Test the Figma MCP connection and list available tools"
   ```
4. Verify you see tools: `get_code`, `get_variable_defs`, `get_image`

### 5.2 Test Design-to-Code Workflow
1. Select a frame in Figma (e.g., a button component)
2. Copy the Figma link
3. In VS Code Agent chat, prompt:
   ```
   "Generate React + TypeScript + Tailwind code for this Figma component: [FIGMA_LINK]
   Use components from src/components/ui and follow DSR design system patterns"
   ```
4. Verify the output includes:
   - Proper TypeScript interfaces
   - Tailwind CSS classes matching DSR design tokens
   - Component structure compatible with existing DSR patterns

### 5.3 Test Variable Extraction
1. Select a complex component with multiple design tokens
2. Prompt:
   ```
   "Extract all design variables and tokens used in my current Figma selection"
   ```
3. Verify the output includes:
   - Color variable names and values
   - Spacing tokens
   - Typography specifications
   - Component variant information

## Step 6: Workflow Integration

### 6.1 Establish Design-to-Code Process
1. **Design Phase**: Create/update designs in Figma with proper variables and components
2. **Code Generation**: Use MCP to generate initial component code
3. **Integration**: Integrate generated code with existing DSR component library
4. **Testing**: Verify component works with all 7 microservices
5. **Documentation**: Update component documentation and design system

### 6.2 Team Training Checklist
- [ ] Train designers on Figma variable setup and Code Connect
- [ ] Train developers on MCP workflow and prompt engineering
- [ ] Establish code review process for MCP-generated components
- [ ] Document DSR-specific prompting patterns and best practices

## Troubleshooting

### Common Issues and Solutions

**Issue**: MCP tools not appearing in VS Code
- **Solution**: Restart both Figma Desktop App and VS Code, verify MCP server is running

**Issue**: Generated code doesn't match DSR patterns
- **Solution**: Improve prompts with specific DSR component references and design system guidelines

**Issue**: Design tokens not extracting correctly
- **Solution**: Verify Figma variables are properly set up with code syntax

**Issue**: Code Connect not working
- **Solution**: Ensure Code Connect is enabled in Figma MCP settings and components are properly mapped

## Next Steps

1. **Complete Setup**: Follow all steps in this guide
2. **Create DSR Design System**: Organize Figma files according to DSR structure
3. **Generate Enhanced Components**: Use MCP to create improved versions of existing components
4. **Test Integration**: Verify all components work with DSR backend services
5. **Document Workflow**: Create team documentation for ongoing use

## Support Resources

- **Figma MCP Documentation**: https://help.figma.com/hc/en-us/articles/32132100833559
- **DSR Component Library**: `frontend/src/components/`
- **Design System Documentation**: `DSR_DESIGN_ANALYSIS_FRAMEWORK.md`
- **Troubleshooting**: Contact development team or refer to Figma support

This setup enables powerful AI-assisted design-to-code workflows while maintaining the robust architecture and functionality of the DSR system.
