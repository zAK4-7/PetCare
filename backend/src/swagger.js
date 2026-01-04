import swaggerJSDoc from 'swagger-jsdoc';

export function makeSwaggerSpec() {
  const options = {
    definition: {
      openapi: '3.0.0',
      info: {
        title: 'PetCare API',
        version: '1.0.0',
        description: 'API REST PetCare (Express + Prisma + SQLite) - JWT roles'
      },
      servers: [{ url: 'http://localhost:' + (process.env.PORT || 3000) }],
      components: {
        securitySchemes: {
          bearerAuth: { type: 'http', scheme: 'bearer', bearerFormat: 'JWT' }
        }
      }
    },
    apis: ['./src/routes/*.js']
  };
  return swaggerJSDoc(options);
}
